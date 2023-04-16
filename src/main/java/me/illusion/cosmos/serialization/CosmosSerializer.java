package me.illusion.cosmos.serialization;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Location;

public interface CosmosSerializer<T extends TemplatedArea> {

    CompletableFuture<byte[]> serialize(T area);
    CompletableFuture<TemplatedArea> deserialize(byte[] data);

    CompletableFuture<TemplatedArea> createArea(Cuboid bounds, Location anchor);

    String getName();

}
