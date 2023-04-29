package me.illusion.cosmos.file;

import me.illusion.cosmos.utilities.storage.YMLBase;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class CosmosDatabasesFile extends YMLBase {

    public CosmosDatabasesFile(JavaPlugin plugin) {
        super(plugin, "databases.yml");
    }

    public ConfigurationSection getDatabase(String name) {
        return getConfiguration().getConfigurationSection("databases." + name);
    }

    public String getDefault() {
        return getConfiguration().getString("default");
    }
}
