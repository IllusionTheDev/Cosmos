package me.illusion.cosmos.template.grid.impl;

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

@Builder
public class WorldPerAreaGrid implements CosmosGrid {

    private final Map<UUID, PooledWorld> worldPool = new ConcurrentHashMap<>();

    @Default
    private int maxActiveWorlds = 25;

    @Default
    private ChunkGenerator chunkGenerator = new VoidGenerator();

    @Default
    private Vector spawnLocation = new Vector(0, 128, 0);

    @Override
    public CompletableFuture<PastedArea> paste(TemplatedArea area) {
        UUID worldId = createWorld();

        getOrCreateWorld(worldId).setState(PooledWorldState.IN_USE);

        return area.paste(new Location(Bukkit.getWorld(worldId), spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ())).thenApply((pastedArea -> {
            ProxyPastedArea proxy = new ProxyPastedArea(pastedArea);

            proxy.setPostUnloadAction(() -> {
                unloadWorld(pastedArea.getPasteLocation().getWorld().getUID());
            });

            return proxy;
        }));
    }

    private void unloadWorld(UUID worldId) {
        getOrCreateWorld(worldId).setState(PooledWorldState.UNUSED);

        attemptUnloadExtraWorlds();
    }

    public void attemptUnloadExtraWorlds() {
        int activeWorlds = 0;

        for(PooledWorld world : worldPool.values()) {
            if(world.getState() == PooledWorldState.IN_USE)
                activeWorlds++;
        }

        int worldsToUnload = activeWorlds - maxActiveWorlds;

        if(worldsToUnload <= 0)
            return;

        List<Map.Entry<UUID, PooledWorld>> worldsToUnloadList = new ArrayList<>();

        for(Map.Entry<UUID, PooledWorld> entry : worldPool.entrySet()) {
            PooledWorld world = entry.getValue();

            if(world.getState() == PooledWorldState.UNUSED)
                worldsToUnloadList.add(entry);

            if(worldsToUnloadList.size() >= worldsToUnload)
                break;
        }

        for(Map.Entry<UUID, PooledWorld> entry : worldsToUnloadList) {
            Bukkit.unloadWorld(entry.getValue().getWorldName(), false);
            getOrCreateWorld(entry.getKey()).setState(PooledWorldState.UNLOADED);
        }
    }


    private UUID createWorld() {
        // Let's see if we have any unused or unloaded worlds in our pool;
        List<Map.Entry<UUID, PooledWorld>> unusedWorlds = new ArrayList<>();

        for(Map.Entry<UUID, PooledWorld> entry : worldPool.entrySet()) {
            PooledWorld world = entry.getValue();

            if(world.getState() == PooledWorldState.UNUSED || world.getState() == PooledWorldState.UNLOADED)
                unusedWorlds.add(entry);
        }

        if(!unusedWorlds.isEmpty()) {
            // We prefer to use unused worlds, but if we don't have any, we'll use unloaded worlds
            // We can sort by ordinal, the higher the ordinal, the more "used" the world is
            unusedWorlds.sort((o1, o2) -> o2.getValue().state.ordinal() - o1.getValue().state.ordinal());

            Map.Entry<UUID, PooledWorld> entry = unusedWorlds.get(0);

            UUID worldId = entry.getKey();

            if(entry.getValue().getState() == PooledWorldState.UNLOADED) {
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

        if(created == null)
            throw new IllegalStateException("Failed to create world for area paste");

        created.setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());
        UUID worldId = created.getUID();

        return worldId;
    }

    private PooledWorld getOrCreateWorld(UUID worldId) {
        World world = Bukkit.getWorld(worldId);

        return worldPool.computeIfAbsent(worldId, id -> new PooledWorld(worldId, world == null ? worldId.toString() : world.getName(), PooledWorldState.UNUSED));
    }

    @Data
    @AllArgsConstructor
    private static class PooledWorld {
        private final UUID worldId;
        private final String worldName;

        @Setter
        private PooledWorldState state;
    }

    private enum PooledWorldState {
        IN_USE,
        UNUSED,
        UNLOADED
    }

}
