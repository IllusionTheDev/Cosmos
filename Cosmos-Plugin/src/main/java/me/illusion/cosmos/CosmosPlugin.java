package me.illusion.cosmos;

import lombok.Getter;
import me.illusion.cosmos.cache.CosmosCache;
import me.illusion.cosmos.database.CosmosContainerRegistry;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializerRegistry;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.grid.CosmosGridRegistry;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class CosmosPlugin extends JavaPlugin {

    private CosmosSerializerRegistry serializerRegistry;
    private CosmosGridRegistry gridRegistry;
    private CosmosContainerRegistry containerRegistry;

    private CosmosCache<PastedArea> pasteCache;
    private CosmosCache<TemplatedArea> templateCache;

    @Override
    public void onEnable() {
        // Plugin startup logic
        serializerRegistry = new CosmosSerializerRegistry();
        gridRegistry = new CosmosGridRegistry();
        containerRegistry = new CosmosContainerRegistry(this);

        templateCache = new CosmosCache<>();
        pasteCache = new CosmosCache<>();

        registerDefaults();

        // TODO: Remote sources, testing
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
        containerRegistry.registerDefaults();
    }
}
