package me.illusion.cosmos;

import lombok.Getter;
import me.illusion.cosmos.database.CosmosContainerRegistry;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.serialization.CosmosSerializerRegistry;
import me.illusion.cosmos.template.grid.CosmosGrid;
import me.illusion.cosmos.template.grid.CosmosGridRegistry;
import me.illusion.cosmos.template.grid.impl.WorldPerAreaGrid;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import me.illusion.cosmos.utilities.hook.WorldEditUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class CosmosPlugin extends JavaPlugin {

    private CosmosSerializerRegistry serializerRegistry;
    private CosmosGridRegistry gridRegistry;
    private CosmosContainerRegistry containerRegistry;

    @Override
    public void onEnable() {
        // Plugin startup logic
        serializerRegistry = new CosmosSerializerRegistry();
        gridRegistry = new CosmosGridRegistry();
        containerRegistry = new CosmosContainerRegistry(this);

        registerDefaults();

        // TODO: Remote sources, wiping pooled worlds onDisable, local sources, testing
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        gridRegistry.unloadAll().join();

        for(CosmosDataContainer container : containerRegistry.getContainersAsCollection())
            container.flush().join();
    }

    /**
     * Registers any default data
     */
    public void registerDefaults() {
        serializerRegistry.registerDefaultSerializers();
        containerRegistry.registerDefaults();
    }

    private void test() {
        CosmosGrid grid = WorldPerAreaGrid.builder().build(); // We have defaults for everything else
        gridRegistry.register(grid);

        CosmosSerializer worldEditSerializer = serializerRegistry.get("worldedit");
        CosmosDataContainer fileContainer = containerRegistry.getContainer("file");

        Player player = Bukkit.getOnlinePlayers().iterator().next();

        Cuboid bounds = WorldEditUtils.getPlayerSelection(player);
        Location anchor = player.getLocation();

        worldEditSerializer.createArea(bounds, anchor).thenCompose(area -> fileContainer.saveTemplate("some-arena", area).thenRun(() -> grid.paste(area)));


    }
}
