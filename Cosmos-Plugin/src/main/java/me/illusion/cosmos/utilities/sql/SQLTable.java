package me.illusion.cosmos.utilities.sql;

import me.illusion.cosmos.utilities.sql.connection.SQLConnectionProvider;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SQLTable {

    private final String name;
    private final Map<String, SQLColumn> columns = new HashMap<>();
    private final SQLConnectionProvider provider;

    public SQLTable(String name, SQLConnectionProvider provider) {
        this.name = name;
        this.provider = provider;
    }

    public CompletableFuture<Void> addColumn(ColumnData data) {
        columns.put(name, new SQLColumn(this, data));

        return provider.getConnection().thenAccept(connection -> {
            try {
                // if the column already exists, don't add it
                if (connection.getMetaData().getColumns(null, null, name, data.getName()).next())
                    return;

                Object value = data.getData();

                // value can be the length of a varchar, or the precision of a decimal, or just null
                String valueString = value == null ? "" : "(" + value + ")";

                boolean primary = data.isPrimary();

                if(primary) {
                    valueString += " PRIMARY KEY";
                }

                connection.createStatement().executeUpdate("ALTER TABLE " + this.name + " ADD COLUMN " + data.getName() + " " + data.getType().name() + valueString);
                // mariadb is giving me a headache
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> removeColumn(String name) {
        columns.remove(name);

        return provider.getConnection().thenAccept(connection -> {
            try {
                connection.createStatement().executeUpdate("ALTER TABLE " + this.name + " DROP COLUMN " + name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public SQLColumn getColumn(String name) {
        return columns.get(name);
    }

    public CompletableFuture<Void> createTable() {
        return provider.getConnection().thenAccept(connection -> {
            try {
                connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + name + " (id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (id))");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> createTable(String elements) {
        return provider.getConnection().thenAccept(connection -> {
            try {
                connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + name + "(" + elements + ")");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> deleteTable() {
        return provider.getConnection().thenAccept(connection -> {
            try {
                connection.createStatement().executeUpdate("DROP TABLE " + name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> insert(Map<String, Object> data) {
        return provider.getConnection().thenAccept(connection -> {
            try {
                StringBuilder builder = new StringBuilder();

                for (String key : data.keySet()) {
                    builder.append(key).append(", ");
                }

                String columns = builder.substring(0, builder.length() - 2);

                builder = new StringBuilder();

                for (Object value : data.values()) {
                    if(value instanceof Number) {
                        builder.append(value).append(", ");
                    } else {
                        builder.append("'").append(value).append("', ");
                    }
                }

                String values = builder.substring(0, builder.length() - 2);

                connection.createStatement().executeUpdate("INSERT INTO " + name + " (" + columns + ") VALUES (" + values + ")");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<ResultSet> executeQuery(String query, Object... args) {
        return provider.getConnection().thenApply(connection -> {
            try {
                PreparedStatement statement = connection.prepareStatement(query);

                for (int index = 0; index < args.length; index++) {
                    statement.setObject(index + 1, args[index]);
                }

                if(query.contains("SELECT"))
                    return statement.executeQuery();

                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<Map<String, Object>> fetch(String query, Object... args) {
        return executeQuery(query, args).thenApply(results -> {
            try {
                Map<String, Object> data = new HashMap<>();

                // No clue if all the tables are in the query
                int columnCount = results.getMetaData().getColumnCount();

                while (results.next()) {
                    for (int index = 1; index <= columnCount; index++) {
                        data.put(results.getMetaData().getColumnName(index), results.getObject(index));
                    }
                }

                results.close();

                return data;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

}
