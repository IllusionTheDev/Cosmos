package me.illusion.cosmos.cache;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple cache implementation, that uses a String identifier to store and retrieve values.
 * @param <T> The type of the value
 */
public class CosmosCache<T> {

    private final Map<String, T> cache = new ConcurrentHashMap<>();

    /**
     * Registers a value to the cache.
     * @param identifier The identifier to register the value under
     * @param value The value to register
     */
    public void register(String identifier, T value) {
        cache.put(identifier, value);
    }

    /**
     * Registers a value to the cache, using a future.
     * @param identifier The identifier to register the value under
     * @param valueFuture The future which will complete with the value
     */
    public void register(String identifier, CompletableFuture<T> valueFuture) {
        valueFuture.thenAccept(value -> {
            if (value == null) {
                System.out.println("Failed to load area " + identifier);
                return; // the future failed to load the area, so we'll not cache it
            }

            System.out.println("Cached area " + identifier);
            cache.put(identifier, value);
        });
    }

    /**
     * Gets a value from the cache.
     * @param identifier The identifier to get the value from
     * @return The value, or null if it does not exist
     */
    public T get(String identifier) {
        return cache.get(identifier);
    }

    /**
     * Unregisters a value from the cache.
     * @param identifier The identifier to unregister the value from
     */
    public void unregister(String identifier) {
        cache.remove(identifier);
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
    }

}
