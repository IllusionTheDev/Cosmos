package me.illusion.cosmos.serialization.impl;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.nms.world.SlimeLoadedWorld;
import com.grinderwolf.swm.plugin.loaders.slime.SlimeWorldReaderRegistry;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.impl.SlimeTemplatedArea;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SlimeSerializer implements CosmosSerializer {

    private final CosmosDataContainer container;
    private final SlimePlugin slimePlugin;

    public SlimeSerializer(CosmosDataContainer container) {
        this.container = container;
        this.slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
    }

    @Override
    public CompletableFuture<byte[]> serialize(TemplatedArea area) {
        SlimeWorld world = ((SlimeTemplatedArea) area).getSlimeWorld();
        SlimeLoadedWorld loadedWorld = (SlimeLoadedWorld) world;

        try {
            return loadedWorld.serialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<TemplatedArea> deserialize(byte[] data) {
        SlimeLoader loader = slimePlugin.getLoader("cosmos");
        // we need to paste this world
        return CompletableFuture.supplyAsync(() -> {
            try {
                SlimeWorld world = SlimeWorldReaderRegistry.readWorld(loader, UUID.randomUUID().toString(), data, null, false);
                return new SlimeTemplatedArea(slimePlugin, world);
            } catch (IOException | CorruptedWorldException | NewerFormatException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<TemplatedArea> createArea(Cuboid bounds, Location anchor) {
        return CompletableFuture.supplyAsync(() -> {
            SlimeWorld world = slimePlugin.getWorld(anchor.getWorld().getName()); // the bounds and anchor location can't really be shifted, so we ignore them
            return new SlimeTemplatedArea(slimePlugin, world);
        });
    }


    @Override
    public String getName() {
        return "slime";
    }

}
