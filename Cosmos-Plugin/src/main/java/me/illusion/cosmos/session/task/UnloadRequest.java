package me.illusion.cosmos.session.task;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a request to unload a session. It contains the epoch of when the session should be unloaded, and a future which will complete when the session is
 * unloaded. This class is thread-safe.
 *
 * @author Illusion
 * @see UnloadTask
 */
public class UnloadRequest {

    private final long epoch;
    private final CompletableFuture<Boolean> future;

    public UnloadRequest(long epoch, CompletableFuture<Boolean> future) {
        this.epoch = epoch;
        this.future = future;
    }

    /**
     * Gets the epoch of when the session should be unloaded.
     *
     * @return The epoch
     */
    public long getEpoch() {
        return epoch;
    }

    /**
     * Gets the future which will complete when the session is unloaded.
     *
     * @return The future
     */
    public CompletableFuture<Boolean> getFuture() {
        return future;
    }

    /**
     * Cancels the unload request.
     */
    public void cancel() {
        future.complete(false);
    }

    /**
     * Completes the unload request.
     */
    public void complete() {
        future.complete(true);
    }
}
