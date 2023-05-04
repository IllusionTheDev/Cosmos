package me.illusion.cosmos.metrics.database;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.utilities.time.Time;
import org.bukkit.configuration.ConfigurationSection;

public interface MetricsDatabase {

    CompletableFuture<Boolean> enable(ConfigurationSection section);

    Time getUpdateInterval();

    String getName();

    CompletableFuture<Void> pushMetric(String metricId, Map<String, Integer> data);
}
