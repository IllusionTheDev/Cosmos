package me.illusion.cosmos.template;

import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Location;

/**
 * A templated area is a "blueprint" of a map, which can be pasted at any given location. To create a templated area, use a {@link CosmosSerializer}.
 * <p>
 *
 * @author Illusion
 */
public interface TemplatedArea {

    /**
     * Pastes the area at the given location, and returns a CompletableFuture which will be completed when the paste is done. Depending on the serializer, the
     * future may be completed immediately, and the area will be pasted in the main thread (WorldEdit).
     *
     * @param location The location to paste the area at
     * @return A future of a Pasted Area, which can be used to get the location of the area, and to undo the paste.
     */
    CompletableFuture<PastedArea> paste(Location location);

    /**
     * Obtains the dimensions of the area.
     *
     * @return The dimensions of the area
     */
    Cuboid getDimensions();

    /**
     * Obtains the serializer used to create this area.
     *
     * @return The serializer used to create this area
     */
    CosmosSerializer getSerializer();

}
