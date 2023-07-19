package me.illusion.cosmos.world.pool;

public enum PooledWorldState implements Comparable<PooledWorldState> {
    IN_USE,
    UNUSED,
    UNLOADED;

    public boolean isLoaded() {
        return this == IN_USE || this == UNUSED;
    }
}