package me.illusion.cosmos.metrics;

import java.util.Map;

public interface CosmosMetric {

    String getName();

    Map<String, Integer> fetchMetrics();

    default boolean supportsAsync() {
        return false;
    }

}
