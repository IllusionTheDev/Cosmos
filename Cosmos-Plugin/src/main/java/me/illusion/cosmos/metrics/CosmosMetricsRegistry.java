package me.illusion.cosmos.metrics;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.metrics.database.MetricsDatabase;
import me.illusion.cosmos.metrics.database.impl.InfluxMetricsDatabase;
import me.illusion.cosmos.metrics.impl.LoadedSessionsMetric;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

/**
 * This class is responsible for managing metrics and pushing them to the database, as well as providing a way to register your own databases to push metrics
 * to.
 *
 * @author Illusion
 */
public class CosmosMetricsRegistry {

    private final Map<String, CosmosMetric> metrics = new ConcurrentHashMap<>();
    private final Map<String, MetricsDatabase> databases = new ConcurrentHashMap<>();
    private final CosmosPlugin plugin;
    private boolean allowsRegistration = true;
    private MetricsDatabase metricsDatabase;

    public CosmosMetricsRegistry(CosmosPlugin plugin) {
        this.plugin = plugin;
        registerDefaults();
    }

    /**
     * Attempts to enable the default database specified in the metrics file. If the database is not found, or the database fails to enable, this method will
     * return false.
     *
     * @return A completable future that completes when the database is enabled.
     */
    public CompletableFuture<Boolean> enable() {
        allowsRegistration = false;
        boolean enabled = plugin.getMetricsFile().isEnabled();

        if (!enabled) {
            return CompletableFuture.completedFuture(false);
        }

        String type = plugin.getMetricsFile().getDefaultType();
        MetricsDatabase database = databases.get(type);

        if (database == null) {
            return CompletableFuture.completedFuture(false);
        }

        ConfigurationSection section = plugin.getMetricsFile().getDatabaseSection(type);
        database.enable(section).thenAccept(result -> {
            if (result) {
                metricsDatabase = database;
                initMetricsTimer();
            }

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });

        return CompletableFuture.completedFuture(true);
    }

    /**
     * Initializes the metrics timer. This timer will push metrics to the database at the specified interval. There are two timers, one async and one sync. The
     * async timer will push metrics that support async fetching,
     */
    private void initMetricsTimer() {
        long intervalTicks = metricsDatabase.getUpdateInterval().asTicks();

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> { // We have both an async timer and a sync timer because why not
            for (CosmosMetric metric : metrics.values()) {
                if (!metric.supportsAsync()) {
                    continue;
                }

                pushMetric(metric);
            }
        }, intervalTicks, intervalTicks);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (CosmosMetric metric : metrics.values()) {
                if (metric.supportsAsync()) {
                    continue;
                }

                pushMetric(metric);
            }
        }, intervalTicks, intervalTicks);
    }

    /**
     * Pushes a metric to the database.
     *
     * @param metric The metric to push.
     * @return A completable future that completes when the metric is pushed.
     */
    public CompletableFuture<Void> pushMetric(CosmosMetric metric) {
        if (metricsDatabase == null) {
            return CompletableFuture.completedFuture(null);
        }

        return metricsDatabase.pushMetric(metric.getName(), metric.fetchMetrics());
    }

    /**
     * Registers a metric to the registry, allowing it to be constantly pushed to the database by the metrics timer.
     *
     * @param metric The metric to register.
     */
    public void registerMetric(CosmosMetric metric) {
        metrics.put(metric.getName(), metric);
    }

    /**
     * Registers a database to the registry, allowing it to be used as the metrics database.
     *
     * @param database The database to register.
     */
    public void registerDatabase(MetricsDatabase database) {
        if (!allowsRegistration) {
            throw new IllegalStateException("Cannot register databases after metrics have been enabled! (Try registering them onEnable instead)");
        }

        databases.put(database.getName(), database);
    }

    /**
     * Registers the default databases and metrics.
     */
    public void registerDefaults() {
        registerDatabase(new InfluxMetricsDatabase());

        registerMetric(new LoadedSessionsMetric(plugin));
    }

    /**
     * Gets the metrics database.
     *
     * @return The metrics database.
     */
    public MetricsDatabase getMetricsDatabase() {
        return metricsDatabase;
    }
}
