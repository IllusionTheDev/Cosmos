package me.illusion.cosmos.utilities.concurrency;

import java.util.concurrent.CompletableFuture;

public class FutureUtils {

    /**
     * Omit the type of future
     *
     * @param future the future
     * @param <T>    the type of the future
     * @return a future that completes when the given future completes
     */
    public static <T> CompletableFuture<Void> omitType(CompletableFuture<T> future) {
        return future.thenAccept(t -> {
        });
    }

}
