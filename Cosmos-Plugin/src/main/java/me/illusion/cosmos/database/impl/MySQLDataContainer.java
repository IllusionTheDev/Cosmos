package me.illusion.cosmos.database.impl;

import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.base.SQLDataContainer;
import me.illusion.cosmos.utilities.sql.connection.MySQLConnectionProvider;
import me.illusion.cosmos.utilities.sql.connection.SQLConnectionProvider;
import org.bukkit.configuration.ConfigurationSection;

public class MySQLDataContainer extends SQLDataContainer {

    public MySQLDataContainer(CosmosPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "mysql";
    }

    @Override
    public SQLConnectionProvider getSQLConnectionProvider(ConfigurationSection section) {
        return new MySQLConnectionProvider(section);
    }
}
