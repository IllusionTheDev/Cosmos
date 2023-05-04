package me.illusion.cosmos.metrics.impl;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.metrics.CosmosMetric;
import me.illusion.cosmos.session.CosmosSessionHolder;

public class LoadedSessionsMetric implements CosmosMetric {

    private final CosmosPlugin plugin;

    public LoadedSessionsMetric(CosmosPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "loaded_sessions";
    }

    @Override
    public Map<String, Integer> fetchMetrics() {
        ImmutableMap<String, CosmosSessionHolder> holders = plugin.getSessionHolderRegistry().getHolders();

        Map<String, Integer> metrics = new HashMap<>();

        for (Map.Entry<String, CosmosSessionHolder> entry : holders.entrySet()) {
            String name = entry.getKey();
            CosmosSessionHolder holder = entry.getValue();

            metrics.put(name, holder.getSessionCount());
        }

        return metrics;
    }
}
