package me.illusion.cosmos.database.impl;

import java.io.File;
import java.util.Map;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.base.CosmosSQLQuery;
import me.illusion.cosmos.database.base.SQLDataContainer;
import me.illusion.cosmos.utilities.sql.connection.SQLConnectionProvider;
import me.illusion.cosmos.utilities.sql.connection.SQLiteConnectionProvider;
import org.bukkit.configuration.ConfigurationSection;

public class SQLiteDataContainer extends SQLDataContainer {

    private static final String FETCH_TEMPLATE = "SELECT * FROM %s WHERE template_id = ?";
    private static final String SAVE_TEMPLATE = "INSERT OR REPLACE INTO %s (template_id, template_serializer, template_data) VALUES (?, ?, ?);";
    private static final String DELETE_TEMPLATE = "DELETE FROM %s WHERE template_id = ?";
    private static final String FETCH_ALL = "SELECT * FROM %s";

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
    protected Map<CosmosSQLQuery, String> getQueries() {
        return Map.of(
            CosmosSQLQuery.FETCH_ALL, FETCH_ALL,
            CosmosSQLQuery.FETCH_TEMPLATE, FETCH_TEMPLATE,
            CosmosSQLQuery.STORE_TEMPLATE, SAVE_TEMPLATE,
            CosmosSQLQuery.DELETE_TEMPLATE, DELETE_TEMPLATE
        );
    }

    @Override
    public boolean requiresCredentials() {
        return false;
    }
}
