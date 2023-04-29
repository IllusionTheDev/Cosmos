package me.illusion.cosmos.utilities.sql.connection;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import me.illusion.cosmos.utilities.sql.SQLTable;

public class SQLiteConnectionProvider implements SQLConnectionProvider {

    private final File file;
    private final AtomicReference<Connection> connection = new AtomicReference<>();

    public SQLiteConnectionProvider(File file) {
        this.file = file;

        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public CompletableFuture<Connection> getConnection() {
        Connection current = connection.get();

        if(current == null) {
            return createConnection();
        }

        // if the current is not valid, create a new one
        return validateConnection(current).thenCompose(valid -> {
            if(valid) {
                return CompletableFuture.completedFuture(current);
            }

            return createConnection();
        });
    }

    public SQLTable getOrCreateTable(String name) {
        return new SQLTable(name, this);
    }

    private CompletableFuture<Boolean> validateConnection(Connection connection) {
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
                Class.forName("org.sqlite.JDBC");
                return DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }).thenAccept(connection::set).thenApply(v -> connection.get());
    }
}
