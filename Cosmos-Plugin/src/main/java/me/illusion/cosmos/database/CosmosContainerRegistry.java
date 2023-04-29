package me.illusion.cosmos.database;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Setter;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.impl.FileDataContainer;
import me.illusion.cosmos.database.impl.MongoDataContainer;
import me.illusion.cosmos.database.impl.MySQLDataContainer;
import me.illusion.cosmos.database.impl.SQLiteDataContainer;
import me.illusion.cosmos.file.CosmosDatabasesFile;
import org.bukkit.configuration.ConfigurationSection;

/**
 * A registry for all data containers.
 */
public class CosmosContainerRegistry {

    private final Map<String, CosmosDataContainer> containers = new ConcurrentHashMap<>();
    private final CosmosPlugin cosmosPlugin;

    @Setter
    private String defaultContainerId = "file";

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
        registerContainer(new MySQLDataContainer(cosmosPlugin));
        registerContainer(new SQLiteDataContainer(cosmosPlugin));
        registerContainer(new MongoDataContainer(cosmosPlugin));
    }

    /**
     * Initializes the default container.
     * @return A completable future that will complete when the container is initialized
     */
    public CompletableFuture<CosmosDataContainer> initializeDefaultContainer() {
        CosmosDatabasesFile databasesFile = cosmosPlugin.getDatabasesFile();

        String defaultId = databasesFile.getDefault();

        if (defaultId == null) {
            defaultId = defaultContainerId;
        }

        return attemptInitializeContainer(defaultId);
    }

    /**
     * Attempts to initialize a container, and if it fails, it will attempt to initialize the fallback container.
     * A fallback container is a container that is specified in the databases.yml file, and is used when the default container fails to initialize.
     * If no fallback container is specified, the default container (file) will be used.
     * @param id The id of the container to initialize
     * @return A completable future that will complete when the container is initialized
     */
    public CompletableFuture<CosmosDataContainer> attemptInitializeContainer(String id) {
        CosmosDataContainer container = getContainer(id);

        if (container == null) {
            return attemptInitializeContainer(defaultContainerId);
        }

        ConfigurationSection section = cosmosPlugin.getDatabasesFile().getDatabase(id);

        if (section == null && container.requiresCredentials()) {
            return attemptInitializeContainer(defaultContainerId);
        }

        return container.enable(section).thenCompose((success) -> {
            if(!success) {
                cosmosPlugin.getLogger().warning("Failed to initialize container " + id + ". Attempting to initialize fallback container.");
                String fallbackId = section == null ? defaultContainerId : section.getString("fallback", defaultContainerId);
                return attemptInitializeContainer(fallbackId);
            }

            defaultContainerId = id;
            return CompletableFuture.completedFuture(container);
        });
    }

    /**
     * Obtains the default container.
     * @return The default container
     */
    public CosmosDataContainer getDefaultContainer() {
        return getContainer(defaultContainerId);
    }

}
