package me.illusion.cosmos.database.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.sql.ColumnData;
import me.illusion.cosmos.utilities.sql.ColumnType;
import me.illusion.cosmos.utilities.sql.SQLTable;
import me.illusion.cosmos.utilities.sql.connection.SQLConnectionProvider;
import org.bukkit.configuration.ConfigurationSection;

public abstract class SQLDataContainer implements CosmosDataContainer {

    private static final ColumnData[] COLUMNS = new ColumnData[]{
        new ColumnData("template_id", ColumnType.MEDIUMTEXT, null, true),
        new ColumnData("template_serializer", ColumnType.TINYTEXT, 255),
        new ColumnData("template_data", ColumnType.MEDIUMBLOB)};

    private static final Pattern SQL_VALID = Pattern.compile("[a-zA-Z0-9_]");

    private final List<CompletableFuture<?>> runningFutures = new ArrayList<>();
    private final CosmosPlugin plugin;

    private String tableName;
    private SQLTable templatesTable;
    private SQLConnectionProvider provider = null;
    private Map<CosmosSQLQuery, String> queries;

    public SQLDataContainer(CosmosPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<TemplatedArea> fetchTemplate(String name) {
        CompletableFuture<TemplatedArea> future = new CompletableFuture<>();
        CompletableFuture<Void> queryFuture = templatesTable.fetch(queries.get(CosmosSQLQuery.FETCH_TEMPLATE).formatted(tableName), name)
            .thenAccept(results -> {
                if (results == null || results.isEmpty()) {
                    future.complete(null);
                    return;
                }

                for (Map<String, Object> result : results) {
                    System.out.println(result);
                    for (String key : result.keySet()) {
                        System.out.println(key + " : " + result.get(key) + "(" + result.get(key).getClass() + ")");
                    }
                }

                String serializer = (String) results.get(0).get("template_serializer");
                byte[] data = (byte[]) results.get(0).get("template_data");

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

        registerFuture(queryFuture);
        return registerFuture(future);
    }

    @Override
    public CompletableFuture<Void> saveTemplate(String name, TemplatedArea area) {
        return registerVoidFuture(area.getSerializer().serialize(area).thenCompose((contents) -> {
            return templatesTable.executeQuery(
                queries.get(CosmosSQLQuery.STORE_TEMPLATE).formatted(tableName),
                name,
                area.getSerializer().getName(),
                contents
            );
        }));
    }

    @Override
    public CompletableFuture<Void> deleteTemplate(String name) {
        return registerVoidFuture(templatesTable.executeQuery(queries.get(CosmosSQLQuery.DELETE_TEMPLATE).formatted(tableName), name));
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
        queries = getQueries();

        System.out.println("Provider: " + provider);

        tableName = section == null ? "cosmos_templates" : section.getString("table", "cosmos_templates");

        if (!SQL_VALID.matcher(tableName).find()) {
            plugin.getLogger().warning("Invalid SQL table name: " + tableName + " (suspected of attempting SQL Injection). Using default table name instead.");
            tableName = "cosmos_templates";
        }

        System.out.println("Table name: " + tableName);
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
                System.out.println("Could not connect to database");
                return CompletableFuture.completedFuture(false);
            }

            System.out.println("Connected to database");
            return createTable().thenApply(v -> true);
        });
    }

    private CompletableFuture<Void> createTable() {
        return provider.getConnection().thenCompose(connection -> {
            templatesTable = provider.getOrCreateTable(tableName);

            List<CompletableFuture<?>> futures = new ArrayList<>();

            for (ColumnData column : COLUMNS) {
                System.err.println("Creating column " + column.getName());
                futures.add(templatesTable.addColumn(column));
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            }).thenCompose(irrelevant -> templatesTable.createTable());
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<Collection<String>> fetchAllTemplates() {
        CompletableFuture<Collection<String>> future = new CompletableFuture<>();

        CompletableFuture<Void> queryFuture = templatesTable.fetch(queries.get(CosmosSQLQuery.FETCH_ALL).formatted(tableName)).thenAccept(results -> {
            if (results == null || results.isEmpty()) {
                future.complete(null);
                return;
            }

            List<String> names = new ArrayList<>();

            for (Map<String, Object> result : results) {
                names.add((String) result.get("template_id"));
            }

            future.complete(names);
        });

        registerFuture(queryFuture);
        return registerFuture(future);
    }

    private <T> CompletableFuture<T> registerFuture(CompletableFuture<T> future) {
        future.thenRun(() -> runningFutures.remove(future));
        future.exceptionally(throwable -> {
            runningFutures.remove(future);
            throwable.printStackTrace();
            return null;
        });

        runningFutures.add(future);

        return future;
    }

    private CompletableFuture<Void> registerVoidFuture(CompletableFuture<?> future) {
        return registerFuture(future.thenApply(irrelevant -> null));
    }

    @Override
    public boolean requiresCredentials() {
        return true;
    }

    public abstract SQLConnectionProvider getSQLConnectionProvider(ConfigurationSection section);

    protected abstract Map<CosmosSQLQuery, String> getQueries();

}
