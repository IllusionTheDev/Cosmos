package me.illusion.cosmos.serialization;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Location;

/**
 * A serializer is an object responsible for serializing and deserializing areas. An example of a serializer is the WorldEdit serializer, which serializes areas
 * to WorldEdit's Schematic format.
 * <p>
 *
 * @author Illusion
 */
public interface CosmosSerializer {

    /**
     * Serializes an area to a byte array, which can be stored in a database.
     *
     * @param area The area to serialize
     * @return A future of the serialized data
     */
    CompletableFuture<byte[]> serialize(TemplatedArea area);

    /**
     * Deserializes an area from a byte array. Make sure to use the same serializer as the one used to serialize the area, or else it will not work.
     *
     * @param data The data to deserialize
     * @return A future of the deserialized area
     */
    CompletableFuture<TemplatedArea> deserialize(byte[] data);

    /**
     * Creates a new area from a cuboid and an anchor location. (The anchor location is the location of the area's origin)
     *
     * @param bounds The bounds of the area
     * @param anchor The anchor location of the area
     * @return A future of the created area
     */
    CompletableFuture<TemplatedArea> createArea(Cuboid bounds, Location anchor);

    /**
     * Attempts to import an area from a file. This method should return null if the file is not supported.
     *
     * @param file The file to import from
     * @return A future of the imported area
     */
    CompletableFuture<TemplatedArea> tryImport(File file);

    /**
     * Obtains the name of the serializer.
     *
     * @return The name of the serializer
     */
    String getName();

}
