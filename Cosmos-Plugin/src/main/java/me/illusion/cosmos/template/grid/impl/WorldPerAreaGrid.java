package me.illusion.cosmos.template.grid.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.Setter;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.grid.CosmosGrid;
import me.illusion.cosmos.template.impl.quirky.ProxyPastedArea;
import me.illusion.cosmos.world.VoidGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.Vector;

/**
 * A world per area grid is a grid which pastes areas in a new world for each area. Worlds are pooled, and are reused when possible. You can fine tune the
 * amount of worlds that are kept in memory, and the amount of worlds that are kept on disk.
 */
@Builder
public class WorldPerAreaGrid implements CosmosGrid {

    private final Map<UUID, PooledWorld> worldPool = new ConcurrentHashMap<>();

    @Default
    private int maxActiveWorlds = 25;

    @Default
    private int maxUnloadedWorlds = 25; // We'll delete worlds after this

    @Default
    private ChunkGenerator chunkGenerator = new VoidGenerator();

    @Default
    private Vector spawnLocation = new Vector(0, 128, 0);

    @Override
    public CompletableFuture<PastedArea> paste(TemplatedArea area) {
        if (area == null) {
            throw new IllegalArgumentException("Area cannot be null");
        }

        UUID worldId = createWorld();

        getOrCreateWorld(worldId).setState(PooledWorldState.IN_USE);

        return area.paste(new Location(Bukkit.getWorld(worldId), spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ()))
            .thenApply((pastedArea -> {
                ProxyPastedArea proxy = new ProxyPastedArea(pastedArea);

                proxy.setPostUnloadAction(() -> unloadWorld(pastedArea.getPasteLocation().getWorld().getUID()));

                return proxy;
            }));
    }

    @Override
    public CompletableFuture<Void> unloadAll() { // FIXME: This is not deleting worlds, maybe because the files are still in use?
        if (worldPool.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map.Entry<UUID, PooledWorld> entry : worldPool.entrySet()) {
            PooledWorld world = entry.getValue();

            if (world.getState() == PooledWorldState.IN_USE || world.getState() == PooledWorldState.UNUSED) {
                World bukkitWorld = Bukkit.getWorld(world.getWorldName());

                if (bukkitWorld != null) {
                    bukkitWorld.setAutoSave(false); // We don't want to save the world when we unload it
                }

                Bukkit.unloadWorld(world.getWorldName(), false);
                getOrCreateWorld(entry.getKey()).setState(PooledWorldState.UNLOADED);

                futures.add(CompletableFuture.runAsync(() -> new File(Bukkit.getWorldContainer(), world.getWorldName()).delete()));
            }
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Sets the internal state of the world as UNUSED, and attempts to unload any worlds that might not be in use.
     *
     * @param worldId the world ID to unload
     */
    private void unloadWorld(UUID worldId) {
        getOrCreateWorld(worldId).setState(PooledWorldState.UNUSED);

        attemptUnloadExtraWorlds();
    }

    /**
     * Attempts to unload any worlds that are not in use. A world is "in use" if its internal state is IN_USE, or if it fits within the maxActiveWorlds limit.
     * The order in which worlds are unloaded is not guaranteed. This method is called automatically when a world is unloaded.
     */
    public void attemptUnloadExtraWorlds() {
        int activeWorlds = 0;

        for (PooledWorld world : worldPool.values()) {
            if (world.getState() == PooledWorldState.IN_USE) {
                activeWorlds++;
            }
        }

        int worldsToUnload = activeWorlds - maxActiveWorlds;

        if (worldsToUnload <= 0) {
            return;
        }

        List<Map.Entry<UUID, PooledWorld>> worldsToUnloadList = new ArrayList<>();

        for (Map.Entry<UUID, PooledWorld> entry : worldPool.entrySet()) {
            PooledWorld world = entry.getValue();

            if (world.getState() == PooledWorldState.UNUSED) {
                worldsToUnloadList.add(entry);
            }

            if (worldsToUnloadList.size() >= worldsToUnload) {
                break;
            }
        }

        for (Map.Entry<UUID, PooledWorld> entry : worldsToUnloadList) {
            Bukkit.unloadWorld(entry.getValue().getWorldName(), false);
            getOrCreateWorld(entry.getKey()).setState(PooledWorldState.UNLOADED);
        }

        attemptDeleteExtraWorlds();
    }

    /**
     * Attempts to delete any worlds that are unloaded and may not be re-loaded. A world is considered "re-loadable" if its internal state is UNLOADED, or if it
     * fits within the maxUnloadedWorlds limit. The order in which worlds are deleted is not guaranteed. This method is called automatically when a world is
     * unloaded. The internal world files are deleted asynchronously.
     */
    public void attemptDeleteExtraWorlds() {
        int unloadedWorlds = 0;

        for (PooledWorld world : worldPool.values()) {
            if (world.getState() == PooledWorldState.UNLOADED) {
                unloadedWorlds++;
            }
        }

        int worldsToDelete = unloadedWorlds - maxUnloadedWorlds;

        if (worldsToDelete <= 0) {
            return;
        }

        List<Map.Entry<UUID, PooledWorld>> worldsToDeleteList = new ArrayList<>();

        for (Map.Entry<UUID, PooledWorld> entry : worldPool.entrySet()) {
            PooledWorld world = entry.getValue();

            if (world.getState() == PooledWorldState.UNLOADED) {
                worldsToDeleteList.add(entry);
            }

            if (worldsToDeleteList.size() >= worldsToDelete) {
                break;
            }
        }

        for (Map.Entry<UUID, PooledWorld> entry : List.copyOf(worldPool.entrySet())) {
            CompletableFuture.runAsync(() -> new File(Bukkit.getWorldContainer(), entry.getValue().getWorldName()).delete());

            worldPool.remove(entry.getKey());
        }
    }

    /**
     * Creates a new world, or re-uses an existing one. It favors re-using worlds that are already loaded, but if none are available, it will attempt to load an
     * unloaded world, or create a new one.
     *
     * @return the world ID of the world that was created or re-used
     */
    private UUID createWorld() {
        // Let's see if we have any unused or unloaded worlds in our pool;
        List<Map.Entry<UUID, PooledWorld>> unusedWorlds = new ArrayList<>();

        for (Map.Entry<UUID, PooledWorld> entry : worldPool.entrySet()) {
            PooledWorld world = entry.getValue();

            if (world.getState() == PooledWorldState.UNUSED || world.getState() == PooledWorldState.UNLOADED) {
                unusedWorlds.add(entry);
            }
        }

        if (!unusedWorlds.isEmpty()) {
            // We prefer to use unused worlds, but if we don't have any, we'll use unloaded worlds
            // We can sort by ordinal, the higher the ordinal, the more "used" the world is
            unusedWorlds.sort((o1, o2) -> o2.getValue().state.ordinal() - o1.getValue().state.ordinal());

            Map.Entry<UUID, PooledWorld> entry = unusedWorlds.get(0);

            UUID worldId = entry.getKey();

            if (entry.getValue().getState() == PooledWorldState.UNLOADED) {
                World world = Bukkit.createWorld(WorldCreator.name(getOrCreateWorld(worldId).getWorldName()));
                world.setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());

                worldPool.remove(entry.getKey()); // the UID changes
                return world.getUID();
            }

            return entry.getKey();
        }

        UUID randomId = UUID.randomUUID();

        WorldCreator creator = new WorldCreator(randomId.toString());
        creator.generator(chunkGenerator);
        creator.generateStructures(false);

        World created = creator.createWorld();

        if (created == null) {
            throw new IllegalStateException("Failed to create world for area paste");
        }

        created.setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());

        return created.getUID();
    }

    /**
     * Gets a world from the internal registry, or creates a new one if it doesn't exist.
     *
     * @param worldId the world ID
     * @return the world
     */
    private PooledWorld getOrCreateWorld(UUID worldId) {
        World world = Bukkit.getWorld(worldId);

        return worldPool.computeIfAbsent(worldId,
            id -> new PooledWorld(worldId, world == null ? worldId.toString() : world.getName(), PooledWorldState.UNUSED));
    }

    private enum PooledWorldState {
        IN_USE,
        UNUSED,
        UNLOADED
    }

    @Data
    @AllArgsConstructor
    private static class PooledWorld {

        private final UUID worldId;
        private final String worldName;

        @Setter
        private PooledWorldState state;
    }

}
