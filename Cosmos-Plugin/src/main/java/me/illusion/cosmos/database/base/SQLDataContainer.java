package me.illusion.cosmos.database.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.sql.ColumnData;
import me.illusion.cosmos.utilities.sql.ColumnType;
import me.illusion.cosmos.utilities.sql.SQLTable;
import me.illusion.cosmos.utilities.sql.connection.MySQLConnectionProvider;
import me.illusion.cosmos.utilities.sql.connection.SQLConnectionProvider;
import me.illusion.cosmos.utilities.sql.connection.SQLiteConnectionProvider;
import org.bukkit.configuration.ConfigurationSection;

public abstract class SQLDataContainer implements CosmosDataContainer {

    private static final ColumnData[] COLUMNS = new ColumnData[]{
        new ColumnData("template_id", ColumnType.TEXT, null, true),
        new ColumnData("template_serializer", ColumnType.TEXT),
        new ColumnData("template_data", ColumnType.MEDIUMBLOB)
    };

    private static final String FETCH_TEMPLATE = "SELECT * FROM %s WHERE template_id = ?";
    private static final String SAVE_TEMPLATE = "INSERT INTO %s (template_id, template_serializer, template_data) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE template_serializer=VALUES(template_serializer), template_data=VALUES(template_data)";
    private static final String DELETE_TEMPLATE = "DELETE FROM %s WHERE template_id = ?";

    private final List<CompletableFuture<?>> runningFutures = new ArrayList<>();

    private String tableName;
    private SQLTable templatesTable;
    private SQLConnectionProvider provider = null;

    private final CosmosPlugin plugin;

    public SQLDataContainer(CosmosPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<TemplatedArea> fetchTemplate(String name) {
        CompletableFuture<TemplatedArea> future = new CompletableFuture<>();

        CompletableFuture<Void> queryFuture = templatesTable.fetch(FETCH_TEMPLATE.formatted(tableName), name).thenAccept(results -> {
            if (results == null || results.isEmpty()) {
                future.complete(null);
                return;
            }

            String serializer = (String) results.get("template_serializer");
            byte[] data = (byte[]) results.get("template_data");

            CosmosSerializer cosmosSerializer = plugin.getSerializerRegistry().get(serializer);

            if (cosmosSerializer == null) {
                plugin.getLogger().warning("Could not find serializer " + serializer + " for template " + name);
                future.complete(null);
                return;
            }

            System.out.println("Loading template " + name + " with serializer " + serializer);

            // merge these futures without joining
            cosmosSerializer.deserialize(data).thenAccept(future::complete);
        });

        queryFuture.thenRun(() -> runningFutures.remove(queryFuture));
        runningFutures.add(queryFuture);

        future.thenRun(() -> runningFutures.remove(future));
        runningFutures.add(future);

        return future;
    }

    @Override
    public CompletableFuture<Void> saveTemplate(String name, TemplatedArea area) {
        CompletableFuture<Void> queryFuture = templatesTable.executeQuery(
            SAVE_TEMPLATE.formatted(tableName),
            name, area.getSerializer().getName(), area.getSerializer().serialize(area)
        ).thenRun(() -> {}); // map to future<void>

        queryFuture.thenRun(() -> runningFutures.remove(queryFuture));
        runningFutures.add(queryFuture);

        return queryFuture;
    }

    @Override
    public CompletableFuture<Void> deleteTemplate(String name) {
        CompletableFuture<Void> queryFuture = templatesTable.executeQuery(
            DELETE_TEMPLATE.formatted(tableName),
            name
        ).thenRun(() -> {
        }); // map to future<void>

        queryFuture.thenRun(() -> runningFutures.remove(queryFuture));
        runningFutures.add(queryFuture);

        return queryFuture;
    }

    @Override
    public CompletableFuture<Void> flush() {
        return CompletableFuture.allOf(runningFutures.toArray(new CompletableFuture[0]));
    }

    @Override
    public abstract String getName();

    @Override
    public CompletableFuture<Boolean> enable(ConfigurationSection section) {
        provider = getSQLConnectionProvider(section);

        tableName = section.getString("table");

        /*
            table: cosmos_templates
            host: localhost
            port: 3306
            database: cosmos
            username: root
            password: password
         */

        return provider.getConnection().thenCompose(connection -> {
            if (connection == null) {
                return CompletableFuture.completedFuture(false);
            }

            return createTable().thenApply(v -> true);
        });
    }

    private CompletableFuture<Void> createTable() {
        return provider.getConnection().thenCompose(connection -> {
            templatesTable = provider.getOrCreateTable(tableName);

            List<CompletableFuture<?>> futures = new ArrayList<>();

            for (ColumnData column : COLUMNS) {
                futures.add(templatesTable.addColumn(column));
            }

            futures.add(templatesTable.createTable());
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }

    @Override
    public boolean requiresCredentials() {
        return true;
    }

    public abstract SQLConnectionProvider getSQLConnectionProvider(ConfigurationSection section);
}
