package me.illusion.cosmos.session.task;

import java.util.concurrent.CompletableFuture;

public class UnloadRequest {

    private final long epoch;
    private final CompletableFuture<Boolean> future;

    public UnloadRequest(long epoch, CompletableFuture<Boolean> future) {
        this.epoch = epoch;
        this.future = future;
    }

    public long getEpoch() {
        return epoch;
    }

    public CompletableFuture<Boolean> getFuture() {
        return future;
    }

    public void cancel() {
        future.complete(false);
    }

    public void complete() {
        future.complete(true);
    }
}
