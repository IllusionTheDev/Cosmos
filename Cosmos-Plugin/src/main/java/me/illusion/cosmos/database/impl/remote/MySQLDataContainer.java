package me.illusion.cosmos.database.impl.remote;

import java.util.Map;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.base.CosmosSQLQuery;
import me.illusion.cosmos.database.base.SQLDataContainer;
import me.illusion.cosmos.utilities.sql.connection.SQLConnectionProvider;
import me.illusion.cosmos.utilities.sql.connection.impl.MySQLConnectionProvider;
import org.bukkit.configuration.ConfigurationSection;

public class MySQLDataContainer extends SQLDataContainer {

    private static final String FETCH_TEMPLATE = "SELECT * FROM %s WHERE template_id = ?";
    private static final String FETCH_TEMPLATE_SERIALIZER = "SELECT template_serializer FROM %s WHERE template_id = ?";
    private static final String SAVE_TEMPLATE = "INSERT INTO %s (template_id, template_serializer, template_data) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE template_serializer=VALUES(template_serializer), template_data=VALUES(template_data)";
    private static final String DELETE_TEMPLATE = "DELETE FROM %s WHERE template_id = ?";
    private static final String FETCH_ALL = "SELECT * FROM %s";
    private static final String FETCH_ALL_NO_DATA = "SELECT template_id, template_serializer FROM %s";

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
            CosmosSQLQuery.FETCH_TEMPLATE_SERIALIZER, FETCH_TEMPLATE_SERIALIZER,
            CosmosSQLQuery.STORE_TEMPLATE, SAVE_TEMPLATE,
            CosmosSQLQuery.DELETE_TEMPLATE, DELETE_TEMPLATE,
            CosmosSQLQuery.FETCH_ALL_NO_DATA, FETCH_ALL_NO_DATA
        );
    }
}
