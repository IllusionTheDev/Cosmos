package me.illusion.cosmos.utilities.sql.connection;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.utilities.sql.SQLTable;

/**
 * Represents a connection provider for SQL.
 */
public interface SQLConnectionProvider {

    /**
     * Gets a connection to the database. If the connection is not valid, a new one will be created.
     *
     * @return a connection to the database
     */
    CompletableFuture<Connection> getConnection();

    /**
     * Gets or creates a table in the database.
     *
     * @param name the name of the table
     * @return the table
     */
    SQLTable getOrCreateTable(String name);


}
