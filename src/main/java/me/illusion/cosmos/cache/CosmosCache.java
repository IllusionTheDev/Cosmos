package me.illusion.cosmos.cache;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CosmosCache<T> {

    private final Map<String, T> cache = new ConcurrentHashMap<>();

    public void register(String identifier, T value) {
        cache.put(identifier, value);
    }

    public void register(String identifier, CompletableFuture<T> valueFuture) {
        valueFuture.thenAccept(value -> {
            if (value == null) {
                return; // the future failed to load the area, so we'll not cache it
            }

            cache.put(identifier, value);
        });
    }

    public T get(String identifier) {
        return cache.get(identifier);
    }

    public void unregister(String identifier) {
        cache.remove(identifier);
    }

    public void clear() {
        cache.clear();
    }

}
