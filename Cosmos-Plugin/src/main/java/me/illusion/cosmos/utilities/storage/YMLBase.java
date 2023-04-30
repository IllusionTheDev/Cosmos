package me.illusion.cosmos.utilities.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class is responsible for handling the loading and saving of YAML files.
 */
public class YMLBase {

    private final boolean existsOnSource;
    private final JavaPlugin plugin;
    @Getter
    protected File file;
    @Getter
    private FileConfiguration configuration;

    /**
     * Creates a new YMLBase instance with the specified name. It is assumed that the file is embedded in the plugin jar.
     *
     * @param plugin The plugin instance
     * @param name   The name of the file
     */
    public YMLBase(JavaPlugin plugin, String name) {
        this(plugin, new File(plugin.getDataFolder(), name), true);
    }

    /**
     * Creates a new YMLBase instance with the specified file.
     *
     * @param plugin         The plugin instance
     * @param file           The file
     * @param existsOnSource Whether or not the file is embedded in the plugin jar
     */
    public YMLBase(JavaPlugin plugin, File file, boolean existsOnSource) {
        this.plugin = plugin;
        this.file = file;
        this.existsOnSource = existsOnSource;

        this.configuration = loadConfiguration();
    }

    /**
     * Saves the configuration to the file.
     */
    public void save() {
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the configuration from the file.
     *
     * @return The configuration
     */
    private FileConfiguration loadConfiguration() {
        FileConfiguration cfg = new YamlConfiguration();

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            if (existsOnSource) {
                plugin.saveResource(file.getAbsolutePath()
                    .replace(plugin.getDataFolder().getAbsolutePath() + File.separator, ""), false);
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            cfg.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return cfg;
    }

    /**
     * Writes all the values from the source file to the configuration if they do not exist.
     */
    public void writeUnsetValues() {
        if (!existsOnSource) {
            return;
        }

        String inputName = file.getAbsolutePath().replace(plugin.getDataFolder().getAbsolutePath() + File.separator, "");

        try (InputStreamReader reader = new InputStreamReader(plugin.getResource(inputName))) {
            YamlConfiguration source = YamlConfiguration.loadConfiguration(reader);

            for (String key : source.getKeys(true)) {
                if (!configuration.contains(key)) {
                    configuration.addDefault(key, source.get(key));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Reloads the configuration from the file.
     */
    public void reload() {
        configuration = loadConfiguration();
    }
}