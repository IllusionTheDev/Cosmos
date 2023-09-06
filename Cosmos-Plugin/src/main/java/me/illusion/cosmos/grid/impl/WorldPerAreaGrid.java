package me.illusion.cosmos.grid.impl;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.grid.CosmosGrid;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.concurrency.MainThreadExecutor;
import me.illusion.cosmos.world.pool.PooledWorldState;
import me.illusion.cosmos.world.pool.WorldPool;
import me.illusion.cosmos.world.pool.WorldPoolSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * A world per area grid is a grid which pastes areas in a new world for each area. Worlds are pooled, and are reused when possible. You can fine tune the
 * amount of worlds that are kept in memory, and the amount of worlds that are kept on disk.
 */
public class WorldPerAreaGrid implements CosmosGrid {

    private WorldPoolSettings poolSettings = WorldPoolSettings.builder().build();
    private WorldPool worldPool = null; // don't include this in the builder. It's set in the init method.

    public WorldPerAreaGrid(WorldPoolSettings poolSettings) {
        this.poolSettings = poolSettings;
    }

    public WorldPerAreaGrid() {
    }

    @Override
    public void init(CosmosPlugin plugin) {
        worldPool = new WorldPool(plugin, poolSettings);
    }

    @Override
    public CompletableFuture<PastedArea> paste(TemplatedArea area) {
        if (area == null) {
            throw new IllegalArgumentException("Area cannot be null");
        }

        // sanity check
        if (!Bukkit.isPrimaryThread()) {
            System.out.println("Pasting area on async thread");
            CompletableFuture<PastedArea> future = new CompletableFuture<>();
            CompletableFuture.runAsync(() -> paste(area).thenAccept(future::complete), MainThreadExecutor.INSTANCE);
            return future;
        }

        UUID worldId = worldPool.createWorld(true);

        worldPool.setState(worldId, PooledWorldState.IN_USE);

        Vector spawnLocation = worldPool.getSettings().getSpawnLocation();
        return area.paste(new Location(Bukkit.getWorld(worldId), spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ()));
    }

    @Override
    public CompletableFuture<Void> unloadAll() {
        if (worldPool.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return worldPool.unloadAll();
    }

    @Override
    public void registerUnload(PastedArea area) {
        if (area == null) {
            System.err.println("Area cannot be null");
            return;
        }

        UUID worldId = area.getPasteLocation().getWorld().getUID();

        if (!worldPool.containsWorld(worldId)) { // There is no guarantee that the area was pasted by this grid, so we need to check if the world is in the pool
            return;
        }

        worldPool.unloadWorld(worldId);
    }



}
