package me.illusion.cosmos;

import lombok.AccessLevel;
import lombok.Getter;
import me.illusion.cosmos.cache.CosmosCache;
import me.illusion.cosmos.command.CosmosImportCommand;
import me.illusion.cosmos.command.CosmosMigrateCommand;
import me.illusion.cosmos.database.CosmosContainerRegistry;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.file.CosmosDatabasesFile;
import me.illusion.cosmos.listener.CosmosUnloadAreaListener;
import me.illusion.cosmos.serialization.CosmosSerializerRegistry;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.grid.CosmosGridRegistry;
import me.illusion.cosmos.utilities.command.impl.CommandManager;
import me.illusion.cosmos.utilities.concurrency.MainThreadExecutor;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class CosmosPlugin extends JavaPlugin {

    private CosmosSerializerRegistry serializerRegistry;
    private CosmosGridRegistry gridRegistry;
    private CosmosContainerRegistry containerRegistry;
    private CosmosDatabasesFile databasesFile;

    private CosmosCache<PastedArea> pasteCache;
    private CosmosCache<TemplatedArea> templateCache;

    private CommandManager commandManager;
    private MessagesFile messages;

    @Getter(AccessLevel.NONE) // we don't want to expose this to the API
    private Runnable onceInitializedAction = () -> {
    };

    private boolean initialized = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        MainThreadExecutor.init(this);

        databasesFile = new CosmosDatabasesFile(this);
        containerRegistry = new CosmosContainerRegistry(this);
        serializerRegistry = new CosmosSerializerRegistry();
        gridRegistry = new CosmosGridRegistry(this);

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
            System.out.println("Flushing " + container.getName());
            container.flush().join();
            System.out.println("Flushed " + container.getName());
        }
    }

    /**
     * Registers any default data
     */
    public void registerDefaults() {
        serializerRegistry.registerDefaultSerializers();
        containerRegistry.registerDefaults().thenRun(() -> {
            Bukkit.getScheduler().runTask(this, () -> { // make sure we're running after all plugins enable, in case any external plugin registers a container
                containerRegistry.initializeDefaultContainer().thenAccept(container -> {
                    getLogger().info("Initialized default container: " + container.getName());
                    finalizeInitialization();
                });
            });
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
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
        commandManager.register(new CosmosImportCommand(this));
        commandManager.register(new CosmosMigrateCommand(this));
    }

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

    private void finalizeInitialization() {
        initialized = true;
        onceInitializedAction.run();
    }
}
