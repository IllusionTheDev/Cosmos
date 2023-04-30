package me.illusion.cosmos.utilities.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.utilities.sql.connection.SQLConnectionProvider;

/**
 * Represents a table in a SQL database. Make sure to add columns before using the table.
 *
 * @author Illusion
 * @see SQLColumn
 */
public class SQLTable {

    private final String name;
    private final Map<String, SQLColumn> columns = new HashMap<>();
    private final SQLConnectionProvider provider;

    public SQLTable(String name, SQLConnectionProvider provider) {
        this.name = name;
        this.provider = provider;
    }

    /**
     * Adds a column to the table.
     *
     * @param data The data of the column
     * @return A completable future that completes when the column is added
     */
    public CompletableFuture<Void> addColumn(ColumnData data) {
        columns.put(name, new SQLColumn(this, data));

        return provider.getConnection().thenAccept(connection -> {
            try {
                // if the column already exists, don't add it
                if (connection.getMetaData().getColumns(null, null, name, data.getName()).next()) {
                    return;
                }

                Object value = data.getData();

                // value can be the length of a varchar, or the precision of a decimal, or just null
                String valueString = value == null ? "" : "(" + value + ")";

                boolean primary = data.isPrimary();

                if (primary) {
                    valueString += " PRIMARY KEY";
                }

                connection.createStatement()
                    .executeUpdate("ALTER TABLE " + this.name + " ADD COLUMN " + data.getName() + " " + data.getType().name() + valueString);
                // mariadb is giving me a headache
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Removes a column from the table.
     *
     * @param name The name of the column to remove
     * @return A completable future that completes when the column is removed
     */
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

    /**
     * Obtains a column wrapper
     *
     * @param name The name of the column
     * @return The column wrapper
     */
    public SQLColumn getColumn(String name) {
        return columns.get(name);
    }

    /**
     * Creates the table if it doesn't exist.
     *
     * @return A completable future that completes when the table is created
     */
    public CompletableFuture<Void> createTable() {
        return provider.getConnection().thenAccept(connection -> {
            try {
                connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + name + " (id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (id))");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Deletes the table.
     *
     * @return A completable future that completes when the table is deleted
     */
    public CompletableFuture<Void> deleteTable() {
        return provider.getConnection().thenAccept(connection -> {
            try {
                connection.createStatement().executeUpdate("DROP TABLE " + name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Inserts a map of data into the table, where the key is the column name and the value is the data.
     *
     * @param data The data to insert
     * @return A completable future that completes when the data is inserted
     */
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
                    if (value instanceof Number) {
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

    /**
     * Deletes data from the table.
     *
     * @param query The query to execute
     * @param args  The arguments to replace in the query
     * @return A completable future that completes when the data is deleted
     */
    public CompletableFuture<ResultSet> executeQuery(String query, Object... args) {
        return provider.getConnection().thenApply(connection -> {
            try {
                PreparedStatement statement = connection.prepareStatement(query);

                for (int index = 0; index < args.length; index++) {
                    statement.setObject(index + 1, args[index]);
                }

                if (query.contains("SELECT")) {
                    return statement.executeQuery();
                }

                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Fetches data from the table.
     *
     * @param query The query to execute
     * @param args  The arguments to replace in the query
     * @return A completable future that completes when the data is fetched
     */
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
