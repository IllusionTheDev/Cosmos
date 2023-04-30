package me.illusion.cosmos.session.task;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a request to unload a session. It contains the epoch of when the session should be unloaded, and a future which will complete when the session is
 * unloaded. This class is thread-safe.
 *
 * @author Illusion
 * @see UnloadTask
 */
public class UnloadRequest {

    private final UUID sessionId;
    private final long epoch;
    private final CompletableFuture<Boolean> future;

    /**
     * Creates an unload request with the specified session ID, epoch and future.
     *
     * @param sessionId The session ID
     * @param epoch     The epoch
     * @param future    The future
     */
    public UnloadRequest(UUID sessionId, long epoch, CompletableFuture<Boolean> future) {
        this.sessionId = sessionId;
        this.epoch = epoch;
        this.future = future;
    }

    /**
     * Gets the session ID.
     *
     * @return The session ID
     */
    public UUID getSessionId() {
        return sessionId;
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
