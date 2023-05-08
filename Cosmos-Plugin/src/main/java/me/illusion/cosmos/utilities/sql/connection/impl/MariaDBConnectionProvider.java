package me.illusion.cosmos.utilities.sql.connection.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import me.illusion.cosmos.utilities.sql.SQLTable;
import me.illusion.cosmos.utilities.sql.connection.SQLConnectionProvider;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Represents a connection provider for MySQL.
 *
 * @see SQLConnectionProvider
 */
public class MariaDBConnectionProvider implements SQLConnectionProvider {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    private final AtomicReference<Connection> connection = new AtomicReference<>();

    public MariaDBConnectionProvider(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public MariaDBConnectionProvider(ConfigurationSection section) {
        this.host = section.getString("host");
        this.port = section.getInt("port");
        this.database = section.getString("database");
        this.username = section.getString("username");
        this.password = section.getString("password");
    }

    public SQLTable getOrCreateTable(String name) {
        return new SQLTable(name, this);
    }


    @Override
    public CompletableFuture<Connection> getConnection() {
        Connection current = connection.get();

        if (current == null) {
            return createConnection();
        }

        // if the current is not valid, create a new one
        return validateConnection(current).thenCompose(valid -> {
            if (valid) {
                return CompletableFuture.completedFuture(current);
            }

            return createConnection();
        });
    }

    private CompletableFuture<Boolean> validateConnection(Connection connection) {
        if (connection == null) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return connection.isValid(1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    private CompletableFuture<Connection> createConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Class.forName("org.mariadb.jdbc.Driver");
                return DriverManager.getConnection("jdbc:mariadb://" + host + ":" + port + "/" + database, username, password);
            } catch (Exception expected) { // The driver will throw an exception if it fails to connect
                return null;
            }
        }).thenApply(connection -> {
            this.connection.set(connection);
            return connection;
        });
    }
}
