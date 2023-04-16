package me.illusion.cosmos.database;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.impl.FileDataContainer;

/**
 * A registry for all data containers.
 */
public class CosmosContainerRegistry {

    private final Map<String, CosmosDataContainer> containers = new ConcurrentHashMap<>();

    private final CosmosPlugin cosmosPlugin;

    public CosmosContainerRegistry(CosmosPlugin plugin) {
        this.cosmosPlugin = plugin;
    }

    /**
     * Registers a container.
     *
     * @param container The container to register
     */
    public void registerContainer(CosmosDataContainer container) {
        containers.put(container.getName(), container);
    }

    /**
     * Obtains a container by name.
     *
     * @param name The name of the container
     * @return The container, or null if it does not exist
     */
    public CosmosDataContainer getContainer(String name) {
        return containers.get(name);
    }

    /**
     * Obtains all registered containers, as an immutable map.
     *
     * @return An immutable map of all registered containers
     */
    public ImmutableMap<String, CosmosDataContainer> getContainers() {
        return ImmutableMap.copyOf(containers);
    }

    /**
     * Obtains all registered containers, as an immutable collection.
     *
     * @return An immutable collection of all registered containers
     */
    public ImmutableCollection<CosmosDataContainer> getContainersAsCollection() {
        return getContainers().values();
    }

    /**
     * Registers all default containers.
     */
    public void registerDefaults() {
        registerContainer(new FileDataContainer(cosmosPlugin));
    }

}
