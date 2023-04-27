package me.illusion.cosmos.utilities.sql.connection;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

public interface SQLConnectionProvider {

    CompletableFuture<Connection> getConnection();


}
