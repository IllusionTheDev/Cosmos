package me.illusion.cosmos.file;

import me.illusion.cosmos.utilities.storage.YMLBase;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The databases.yml file contains login information for the databases that the templates will be stored in.
 */
public class CosmosDatabasesFile extends YMLBase {

    public CosmosDatabasesFile(JavaPlugin plugin) {
        super(plugin, "databases.yml");
    }

    /**
     * Returns the configuration section for the specified database.
     *
     * @param name the name of the database.
     * @return the configuration section for the specified database.
     */
    public ConfigurationSection getDatabase(String name) {
        return getConfiguration().getConfigurationSection(name);
    }

    /**
     * Obtains the default database specified in the databases.yml file.
     *
     * @return the default database specified in the databases.yml file.
     */
    public String getDefault() {
        return getConfiguration().getString("default");
    }
}
