package me.illusion.cosmos.template.grid;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.template.PastedArea;

public class CosmosGridRegistry {

    private final Set<CosmosGrid> grids = Sets.newConcurrentHashSet();
    private final CosmosPlugin plugin;

    public CosmosGridRegistry(CosmosPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(CosmosGrid grid) {
        grids.add(grid);
        grid.init(plugin);
    }

    public void unregister(CosmosGrid grid) {
        grids.remove(grid);
    }

    public CompletableFuture<Void> unloadAll() {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (CosmosGrid grid : grids) {
            futures.add(grid.unloadAll());
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public void handleUnload(PastedArea area) {
        for (CosmosGrid grid : grids) {
            grid.registerUnload(area);
        }
    }

}
