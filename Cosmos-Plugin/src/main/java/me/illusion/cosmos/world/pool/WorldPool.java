package me.illusion.cosmos.world.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.utilities.time.Time;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.util.Vector;

public class WorldPool {

    private final Map<UUID, PooledWorld> worldPool = new ConcurrentHashMap<>();
    private final Time deletionDelay;

    private final CosmosPlugin plugin;

    @Getter
    private final WorldPoolSettings settings;

    public WorldPool(CosmosPlugin plugin, WorldPoolSettings settings) {
        this.plugin = plugin;
        this.settings = settings;

        this.deletionDelay = settings.getDeletionDelay();

        createBatch(settings.getPreGeneratedWorlds());
    }

    public void createBatch(int amount) {
        if (amount <= 0) {
            return;
        }

        UUID createdWorldId = createWorld(false);
        getOrCreateWorld(createdWorldId).setState(PooledWorldState.UNUSED);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (amount > 1) {
                createBatch(amount - 1);
            }
        }, settings.getBatchDelayTicks());
    }

    /**
     * Sets the internal state of the world as UNUSED, and attempts to unload any worlds that might not be in use.
     *
     * @param worldId the world ID to unload
     */
    public void unloadWorld(UUID worldId) {
        getOrCreateWorld(worldId).setState(PooledWorldState.UNUSED);

        attemptUnloadExtraWorlds();
    }

    /**
     * Attempts to unload any worlds that are not in use. A world is "in use" if its internal state is IN_USE, or if it fits within the maxActiveWorlds limit.
     * The order in which worlds are unloaded is not guaranteed. This method is called automatically when a world is unloaded.
     */
    public void attemptUnloadExtraWorlds() {
        if (!Bukkit.isPrimaryThread()) { // thanks bukkit
            Bukkit.getScheduler().runTask(plugin, this::attemptUnloadExtraWorlds);
            return;
        }

        int unusedWorlds = 0;

        for (PooledWorld world : worldPool.values()) {
            if (world.getState() == PooledWorldState.UNUSED) {
                unusedWorlds++;
            }
        }

        int worldsToUnload = settings.getMaxCachedWorlds() - Math.max(unusedWorlds, settings.getPreGeneratedWorlds());

        if (unusedWorlds < settings.getPreGeneratedWorlds()) { // If we have too few we need to create more
            createBatch(settings.getPreGeneratedWorlds() - unusedWorlds);
            return;
        }

        if (worldsToUnload <= 0) {
            return;
        }

        List<Entry<UUID, PooledWorld>> worldsToUnloadList = new ArrayList<>();

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
            UUID worldId = entry.getKey();
            String name = getOrCreateWorld(worldId).getWorldName();

            Bukkit.unloadWorld(name, false);
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

        int worldsToDelete = unloadedWorlds - settings.getMaxUnloadedWorlds();

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
            PooledWorld world = entry.getValue();

            plugin.getTemporaryWorldHandler().deleteWorld(world.getWorldName(), false).thenRun(() -> {
                worldPool.remove(entry.getKey());
            });
        }
    }

    /**
     * Creates a new world, or re-uses an existing one. It favors re-using worlds that are already loaded, but if none are available, it will attempt to load an
     * unloaded world, or create a new one.
     *
     * @param cache If true, we will attempt to fetch an unused or unloaded world from the pool. If false, we will create a new world immediately.
     * @return the world ID of the world that was created or re-used
     */
    public UUID createWorld(boolean cache) {
        // Let's see if we have any unused or unloaded worlds in our pool;
        List<Map.Entry<UUID, PooledWorld>> unusedWorlds = new ArrayList<>();

        if (cache) {
            for (Map.Entry<UUID, PooledWorld> entry : worldPool.entrySet()) {
                PooledWorld world = entry.getValue();

                if (world.getState() == PooledWorldState.UNUSED || world.getState() == PooledWorldState.UNLOADED) {
                    unusedWorlds.add(entry);
                }
            }
        }

        Vector spawnLocation = settings.getSpawnLocation();

        if (!unusedWorlds.isEmpty()) {
            // We prefer to use unused worlds, but if we don't have any, we'll use unloaded worlds
            // We can sort by ordinal, the higher the ordinal, the more "used" the world is
            unusedWorlds.sort((a, b) -> b.getValue().getState().ordinal() - a.getValue().getState().ordinal());

            Map.Entry<UUID, PooledWorld> entry = unusedWorlds.get(0);

            UUID worldId = entry.getKey();

            if (entry.getValue().getState() == PooledWorldState.UNLOADED) {
                WorldCreator creator = WorldCreator.name(getOrCreateWorld(worldId).getWorldName()).generator(settings.getChunkGenerator())
                    .generateStructures(false);
                World world = plugin.getTemporaryWorldHandler().createWorld(creator);

                world.setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());

                worldPool.remove(entry.getKey()); // the UID changes
                return world.getUID();
            }

            return entry.getKey();
        }

        UUID randomId = UUID.randomUUID();

        WorldCreator creator = new WorldCreator(randomId.toString());
        creator.generator(settings.getChunkGenerator());
        creator.generateStructures(false);

        World created = plugin.getTemporaryWorldHandler().createWorld(creator);

        if (created == null) {
            throw new IllegalStateException("Failed to create world for area paste");
        }

        created.setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());
        created.setAutoSave(false);
        created.setKeepSpawnInMemory(false);

        setState(created.getUID(), PooledWorldState.UNUSED);
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

    /**
     * Checks if the world pool is empty.
     *
     * @return true if the world pool is empty
     */
    public boolean isEmpty() {
        return worldPool.isEmpty();
    }

    /**
     * Checks if the world pool contains a world with the given ID.
     *
     * @param worldId the world ID
     * @return true if the world pool contains the world
     */
    public boolean containsWorld(UUID worldId) {
        return worldPool.containsKey(worldId);
    }

    public CompletableFuture<Void> unloadAll() {
        for (PooledWorld world : worldPool.values()) {
            if (world.getState() == PooledWorldState.UNLOADED) {
                plugin.getTemporaryWorldHandler().deleteWorld(world.getWorldName(), true);
                continue;
            }

            Bukkit.unloadWorld(world.getWorldName(), false);
            world.setState(PooledWorldState.UNLOADED);
        }

        // We can delete unloaded worlds, this is fine

        return CompletableFuture.completedFuture(null);
    }

    public int getWorldCount(PooledWorldState... stateWhitelist) {
        if (stateWhitelist.length == 0 || stateWhitelist.length == PooledWorldState.values().length) {
            return worldPool.size();
        }

        int count = 0;

        for (PooledWorld world : worldPool.values()) {
            for (PooledWorldState state : stateWhitelist) {
                if (world.getState() == state) {
                    count++;
                    break;
                }
            }
        }

        return count;
    }

    public void setState(UUID worldId, PooledWorldState state) {
        getOrCreateWorld(worldId).setState(state);
    }

}
