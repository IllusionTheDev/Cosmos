package me.illusion.cosmos.session;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple session holder registry implementation, that uses a String identifier to store and retrieve session holders. This class is thread-safe. There is no
 * strict requirement to use this class, but any metric about sessions will fail to report without it.
 *
 * @author Illusion
 */
public class CosmosSessionHolderRegistry {

    private final Map<String, CosmosSessionHolder> sessionHolders = new ConcurrentHashMap<>();

    public CosmosSessionHolder getHolder(String name) {
        return sessionHolders.get(name);
    }

    public void registerHolder(String identifier, CosmosSessionHolder holder) {
        sessionHolders.put(identifier, holder);
    }

    public ImmutableMap<String, CosmosSessionHolder> getHolders() {
        return ImmutableMap.copyOf(sessionHolders);
    }

}
