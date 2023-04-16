package me.illusion.cosmos.serialization;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.illusion.cosmos.serialization.impl.WorldEditSerializer;

/**
 * This class is responsible for keeping track
 * of all the serializers that are registered.
 * <p>
 * @author Illusion
 */
public class CosmosSerializerRegistry {

    private final Map<String, CosmosSerializer<?>> serializers = new ConcurrentHashMap<>();

    /**
     * Registers the default serializers. It is recommended
     * that you do not call this method, and instead register
     * your own serializers.
     */
    public void registerDefaultSerializers() {
        register(new WorldEditSerializer());
    }

    /**
     * Registers a serializer.
     * @param serializer The serializer to register
     */
    public void register(CosmosSerializer<?> serializer) {
        serializers.put(serializer.getName(), serializer);
    }

    /**
     * Obtains a serializer by name.
     * @param name The name of the serializer
     * @return The serializer, or null if not found
     */
    public CosmosSerializer<?> get(String name) {
        return serializers.get(name);
    }

    /**
     * Obtains an immutable map of all the registered serializers.
     * @return The map of serializers
     */
    public ImmutableMap<String, CosmosSerializer<?>> getSerializers() {
        return ImmutableMap.copyOf(serializers);
    }

}
