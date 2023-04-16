package me.illusion.cosmos.serialization;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Location;

/**
 * A serializer is an object responsible for serializing and deserializing areas.
 * An example of a serializer is the WorldEdit serializer, which serializes areas to WorldEdit's Schematic format.
 * <p>
 * @author Illusion
 * @param <T> The type of area to serialize
 */
public interface CosmosSerializer<T extends TemplatedArea> {

    /**
     * Serializes an area to a byte array, which can be stored in a database.
     * @param area The area to serialize
     * @return A future of the serialized data
     */
    CompletableFuture<byte[]> serialize(T area);

    /**
     * Deserializes an area from a byte array. Make sure to use the same serializer as the one used to serialize the area, or else it will not work.
     * @param data The data to deserialize
     * @return A future of the deserialized area
     */
    CompletableFuture<TemplatedArea> deserialize(byte[] data);

    /**
     * Creates a new area from a cuboid and an anchor location. (The anchor location is the location of the area's origin)
     * @param bounds The bounds of the area
     * @param anchor The anchor location of the area
     * @return A future of the created area
     */
    CompletableFuture<TemplatedArea> createArea(Cuboid bounds, Location anchor);

    /**
     * Creates a new area from a cuboid and an anchor location. (The anchor location is the location of the area's origin)
     * @return
     */
    String getName();

}
