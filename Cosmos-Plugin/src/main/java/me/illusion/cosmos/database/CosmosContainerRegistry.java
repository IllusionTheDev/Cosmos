package me.illusion.cosmos.database;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Setter;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.impl.FileDataContainer;
import me.illusion.cosmos.database.impl.MemoryDataContainer;
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

    private final Set<String> loadedContainers = Sets.newConcurrentHashSet();
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
    public CompletableFuture<Boolean> registerContainer(CosmosDataContainer container) {
        containers.put(container.getName(), container);

        if (container.requiresCredentials()) {
            ConfigurationSection section = cosmosPlugin.getDatabasesFile().getDatabase(container.getName());

            if (section == null) {
                cosmosPlugin.getLogger().warning("No credentials found for database " + container.getName() + ", disabling...");
                return CompletableFuture.completedFuture(false);
            }

            return container.enable(section).thenApply(result -> {
                if (!result) {
                    cosmosPlugin.getLogger().warning("Failed to enable database " + container.getName() + "!");
                    return result;
                }

                loadedContainers.add(container.getName());

                if (container.getName().equals(defaultContainerId)) {
                    cosmosPlugin.getLogger().info("Default database is " + container.getName() + ".");
                }

                return result;
            });
        }

        loadedContainers.add(container.getName());
        return CompletableFuture.completedFuture(true);
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
    public CompletableFuture<Void> registerDefaults() {
        return CompletableFuture.allOf(
            registerContainer(new MemoryDataContainer()), // I wouldn't use this but whatever
            registerContainer(new FileDataContainer(cosmosPlugin)),
            registerContainer(new MySQLDataContainer(cosmosPlugin)),
            registerContainer(new SQLiteDataContainer(cosmosPlugin)),
            registerContainer(new MongoDataContainer(cosmosPlugin))
        );
    }

    /**
     * Initializes the default container.
     *
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
     * Attempts to initialize a container, and if it fails, it will attempt to initialize the fallback container. A fallback container is a container that is
     * specified in the databases.yml file, and is used when the default container fails to initialize. If no fallback container is specified, the default
     * container (file) will be used.
     *
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

        if (!loadedContainers.contains(id)) {
            String fallback = section == null ? defaultContainerId : section.getString("fallback", defaultContainerId);
            return attemptInitializeContainer(fallback);
        }

        defaultContainerId = id;
        return CompletableFuture.completedFuture(container);
    }

    /**
     * Obtains the default container.
     *
     * @return The default container
     */
    public CosmosDataContainer getDefaultContainer() {
        System.out.println("Default container ID: " + defaultContainerId);
        return getContainer(defaultContainerId);
    }

}
