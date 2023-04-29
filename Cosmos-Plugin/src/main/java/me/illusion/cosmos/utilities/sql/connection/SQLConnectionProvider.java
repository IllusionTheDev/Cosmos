package me.illusion.cosmos.utilities.sql.connection;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.utilities.sql.SQLTable;

public interface SQLConnectionProvider {

    CompletableFuture<Connection> getConnection();
    SQLTable getOrCreateTable(String name);


}
