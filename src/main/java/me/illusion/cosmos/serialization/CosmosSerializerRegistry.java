package me.illusion.cosmos.serialization;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.illusion.cosmos.serialization.impl.WorldEditSerializer;

public class CosmosSerializerRegistry {

    private final Map<String, CosmosSerializer<?>> serializers = new ConcurrentHashMap<>();

    public void registerDefaultSerializers() {
        register(new WorldEditSerializer());
    }

    public void register(CosmosSerializer<?> serializer) {
        serializers.put(serializer.getName(), serializer);
    }

    public CosmosSerializer<?> get(String name) {
        return serializers.get(name);
    }

    public ImmutableMap<String, CosmosSerializer<?>> getSerializers() {
        return ImmutableMap.copyOf(serializers);
    }

}
