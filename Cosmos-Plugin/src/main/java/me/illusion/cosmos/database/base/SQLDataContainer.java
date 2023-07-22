package me.illusion.cosmos.database.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.data.TemplateData;
import me.illusion.cosmos.utilities.sql.ColumnData;
import me.illusion.cosmos.utilities.sql.ColumnType;
import me.illusion.cosmos.utilities.sql.SQLTable;
import me.illusion.cosmos.utilities.sql.connection.SQLConnectionProvider;
import org.bukkit.configuration.ConfigurationSection;

public abstract class SQLDataContainer implements CosmosDataContainer {

    private final ColumnData[] columns = new ColumnData[]{
        new ColumnData("template_id", ColumnType.VARCHAR, 255, true),
        new ColumnData("template_serializer", ColumnType.VARCHAR, 255),
        new ColumnData("template_data", ColumnType.MEDIUMBLOB)
    };

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
        return composeFuture(() -> templatesTable.fetch(queries.get(CosmosSQLQuery.FETCH_TEMPLATE).formatted(tableName), name)
            .thenApply(results -> {
                if (results == null || results.isEmpty()) {
                    return null;
                }

            /* for (Map<String, Object> result : results) {
                System.out.println(result);
                for (String key : result.keySet()) {
                    System.out.println(key + " : " + result.get(key) + "(" + result.get(key).getClass() + ")");
                }
            } */

                String serializer = (String) results.get(0).get("template_serializer");
                byte[] data = (byte[]) results.get(0).get("template_data");

                CosmosSerializer cosmosSerializer = plugin.getSerializerRegistry().get(serializer);

                if (cosmosSerializer == null) {
                    plugin.getLogger().warning("Could not find serializer " + serializer + " for template " + name);
                    return null;
                }

                System.out.println("Loading template " + name + " with serializer " + serializer);

                // merge these futures without joining
                return cosmosSerializer.deserialize(data);
            }));
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

            for (ColumnData column : columns) {
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
        return associateFuture(() -> templatesTable.fetch(queries.get(CosmosSQLQuery.FETCH_ALL).formatted(tableName)).thenApply(results -> {
            if (results == null || results.isEmpty()) {
                return null;
            }

            List<String> names = new ArrayList<>();

            for (Map<String, Object> result : results) {
                names.add((String) result.get("template_id"));
            }

            return names;
        }));
    }

    @Override
    public CompletableFuture<String> fetchTemplateSerializer(String name) {
        return associateFuture(
            () -> templatesTable.fetch(queries.get(CosmosSQLQuery.FETCH_TEMPLATE_SERIALIZER).formatted(tableName), name).thenApply(results -> {
                if (results == null || results.isEmpty()) {
                    return null;
                }

                return (String) results.get(0).get("template_serializer");
            }));
    }

    @Override
    public CompletableFuture<Collection<TemplateData>> fetchAllTemplateData() {
        return associateFuture(() -> templatesTable.fetch(queries.get(CosmosSQLQuery.FETCH_ALL_NO_DATA).formatted(tableName)).thenApply(results -> {

            if (results == null || results.isEmpty()) {
                return null;
            }

            List<TemplateData> data = new ArrayList<>();

            for (Map<String, Object> result : results) {
                String serializer = (String) result.get("template_serializer");
                String id = (String) result.get("template_id");

                data.add(new TemplateData(id, serializer, getName()));
            }

            return data;
        }));
    }


    private <T> CompletableFuture<T> associateTask(Supplier<T> supplier) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier);
        return registerFuture(future);
    }

    private <T> CompletableFuture<T> associateFuture(Supplier<CompletableFuture<T>> supplier) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier).thenCompose(Function.identity());
        return registerFuture(future);
    }

    private CompletableFuture<Void> associateRunnable(Supplier<CompletableFuture<Void>> runnable) {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(runnable).thenCompose(Function.identity());
        return registerFuture(future);
    }

    // I hate this
    private <T> CompletableFuture<T> composeFuture(Supplier<CompletableFuture<CompletableFuture<T>>> future) {
        CompletableFuture<T> f = CompletableFuture.supplyAsync(future).thenCompose(Function.identity()).thenCompose(Function.identity());
        return registerFuture(f);
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

    protected void overrideColumn(String name, ColumnData data) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].getName().equals(name)) {
                columns[i] = data;
                return;
            }
        }
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
