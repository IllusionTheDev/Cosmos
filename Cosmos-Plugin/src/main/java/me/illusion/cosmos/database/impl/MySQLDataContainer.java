package me.illusion.cosmos.database.impl;

import java.util.Map;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.base.CosmosSQLQuery;
import me.illusion.cosmos.database.base.SQLDataContainer;
import me.illusion.cosmos.utilities.sql.connection.MySQLConnectionProvider;
import me.illusion.cosmos.utilities.sql.connection.SQLConnectionProvider;
import org.bukkit.configuration.ConfigurationSection;

public class MySQLDataContainer extends SQLDataContainer {

    private static final String FETCH_TEMPLATE = "SELECT * FROM %s WHERE template_id = ?";
    private static final String SAVE_TEMPLATE = "INSERT INTO %s (template_id, template_serializer, template_data) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE template_serializer=VALUES(template_serializer), template_data=VALUES(template_data)";
    private static final String DELETE_TEMPLATE = "DELETE FROM %s WHERE template_id = ?";
    private static final String FETCH_ALL = "SELECT * FROM %s";

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

    @Override
    protected Map<CosmosSQLQuery, String> getQueries() {
        return Map.of(
            CosmosSQLQuery.FETCH_ALL, FETCH_ALL,
            CosmosSQLQuery.FETCH_TEMPLATE, FETCH_TEMPLATE,
            CosmosSQLQuery.STORE_TEMPLATE, SAVE_TEMPLATE,
            CosmosSQLQuery.DELETE_TEMPLATE, DELETE_TEMPLATE
        );
    }
}
