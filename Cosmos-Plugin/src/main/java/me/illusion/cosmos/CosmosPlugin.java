package me.illusion.cosmos;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.AccessLevel;
import lombok.Getter;
import me.illusion.cosmos.cache.CosmosCache;
import me.illusion.cosmos.command.CosmosImportCommand;
import me.illusion.cosmos.command.CosmosMigrateCommand;
import me.illusion.cosmos.database.CosmosContainerRegistry;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.file.CosmosDatabasesFile;
import me.illusion.cosmos.file.CosmosMetricsFile;
import me.illusion.cosmos.listener.CosmosUnloadAreaListener;
import me.illusion.cosmos.metrics.CosmosMetricsRegistry;
import me.illusion.cosmos.serialization.CosmosSerializerRegistry;
import me.illusion.cosmos.session.CosmosSessionHolderRegistry;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.grid.CosmosGridRegistry;
import me.illusion.cosmos.utilities.command.command.CommandManager;
import me.illusion.cosmos.utilities.concurrency.MainThreadExecutor;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class CosmosPlugin extends JavaPlugin {

    private CosmosSerializerRegistry serializerRegistry;
    private CosmosGridRegistry gridRegistry;
    private CosmosContainerRegistry containerRegistry;
    private CosmosSessionHolderRegistry sessionHolderRegistry;

    private CosmosMetricsRegistry metricsRegistry;

    private CosmosCache<PastedArea> pasteCache;
    private CosmosCache<TemplatedArea> templateCache;

    private CommandManager commandManager;

    private MessagesFile messages;
    private CosmosDatabasesFile databasesFile;
    private CosmosMetricsFile metricsFile;

    @Getter(AccessLevel.NONE) // we don't want to expose this to the API
    private Runnable onceInitializedAction = () -> {
    };

    private boolean initialized = false;
    private final boolean disabled = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        MainThreadExecutor.init(this);

        databasesFile = new CosmosDatabasesFile(this);
        metricsFile = new CosmosMetricsFile(this);

        containerRegistry = new CosmosContainerRegistry(this);
        sessionHolderRegistry = new CosmosSessionHolderRegistry();
        serializerRegistry = new CosmosSerializerRegistry();
        gridRegistry = new CosmosGridRegistry(this);

        metricsRegistry = new CosmosMetricsRegistry(this);

        templateCache = new CosmosCache<>();
        pasteCache = new CosmosCache<>();

        messages = new MessagesFile(this);
        commandManager = new CommandManager(this, messages);

        registerDefaults();
        registerListeners();
        registerCommands();

        // TODO: testing
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("Unloading all grids");
        gridRegistry.unloadAll().join();

        System.out.println("Flushing all containers");
        for (CosmosDataContainer container : containerRegistry.getContainersAsCollection()) {
            if (!containerRegistry.isEnabled(container.getName())) {
                System.out.println("Skipping " + container.getName() + " because it's disabled");
                continue;
            }

            System.out.println("Flushing " + container.getName());
            container.flush().join();
            System.out.println("Flushed " + container.getName());
        }
    }

    /**
     * Registers any default data
     */
    public void registerDefaults() {
        CompletableFuture<Void> containerFuture = new CompletableFuture<>();
        CompletableFuture<Void> metricsFuture = new CompletableFuture<>();

        List<CompletableFuture<?>> futures = List.of(
            // These are all the futures that need to be completed before we can finalize initialization (which lets other plugins know we're ready)
            containerFuture,
            metricsFuture
        );

        serializerRegistry.registerDefaultSerializers();
        containerRegistry.registerDefaults().thenRun(() -> {
            Bukkit.getScheduler().runTask(this, () -> { // make sure we're running after all plugins enable, in case any external plugin registers a container
                containerRegistry.initializeDefaultContainer().thenAccept(container -> {
                    getLogger().info("Initialized default container: " + container.getName());
                    containerFuture.complete(null);
                });
            });
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });

        Bukkit.getScheduler().runTask(this, () -> {
            metricsRegistry.enable().thenRun(() -> metricsFuture.complete(null));
        });

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(this::finalizeInitialization);

    }

    /**
     * Registers any listeners
     */
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new CosmosUnloadAreaListener(this), this);
    }

    /**
     * Registers any commands
     */
    public void registerCommands() {
        commandManager.registerCommand(new CosmosImportCommand(this));
        commandManager.registerCommand(new CosmosMigrateCommand(this));
    }

    /**
     * Registers a runnable to be ran once the plugin is initialized
     *
     * @param runnable the runnable to be ran
     */
    public void onceInitialized(Runnable runnable) {
        if (initialized) {
            runnable.run();
            return;
        }

        // merge
        Runnable old = onceInitializedAction;

        onceInitializedAction = () -> {
            old.run();
            runnable.run();
        };
    }

    /**
     * Finalizes initialization internally
     */
    private void finalizeInitialization() {
        initialized = true;
        onceInitializedAction.run();
    }
}
