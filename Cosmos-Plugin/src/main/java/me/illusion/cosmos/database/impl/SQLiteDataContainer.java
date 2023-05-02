package me.illusion.cosmos.database.impl;

import java.io.File;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.base.SQLDataContainer;
import me.illusion.cosmos.utilities.sql.connection.SQLConnectionProvider;
import me.illusion.cosmos.utilities.sql.connection.SQLiteConnectionProvider;
import org.bukkit.configuration.ConfigurationSection;

public class SQLiteDataContainer extends SQLDataContainer {

    private final File file;

    public SQLiteDataContainer(CosmosPlugin plugin) {
        super(plugin);
        this.file = new File(plugin.getDataFolder(), "database.db");
    }

    @Override
    public String getName() {
        return "sqlite";
    }

    @Override
    public SQLConnectionProvider getSQLConnectionProvider(ConfigurationSection section) {
        return new SQLiteConnectionProvider(file);
    }

    @Override
    public boolean requiresCredentials() {
        return false;
    }
}
