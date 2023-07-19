package me.illusion.cosmos.file;

import me.illusion.cosmos.utilities.storage.YMLBase;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The metrics.yml file contains login information for the metrics system. The metrics system does not actually report to bstats or any similar thing, but
 * instead pushes data to a database specified by the end-user, allowing them to view their own metrics internally through a visualizer like Grafana.
 */
public class CosmosMetricsFile extends YMLBase {

    public CosmosMetricsFile(JavaPlugin plugin) {
        super(plugin, "metrics.yml");
    }

    /**
     * Returns whether the metrics system is enabled.
     *
     * @return whether the metrics system is enabled.
     */
    public boolean isEnabled() {
        return getConfiguration().getBoolean("enabled");
    }

    /**
     * Returns the default database type specified in the metrics.yml file.
     *
     * @return the default database type specified in the metrics.yml file.
     */
    public String getDefaultType() {
        return getConfiguration().getString("default-type");
    }

    /**
     * Returns the configuration section for the specified database type.
     *
     * @param name the name of the database type.
     * @return the configuration section for the specified database type.
     */
    public ConfigurationSection getDatabaseSection(String name) {
        return getConfiguration().getConfigurationSection(name);
    }
}
