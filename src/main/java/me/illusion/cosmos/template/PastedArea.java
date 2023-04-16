package me.illusion.cosmos.template;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;

/**
 * A pasted area is an instance of a {@link TemplatedArea}, which has been pasted at a given location. It can be used to unload the area, and to get the
 * location of the area.
 * <p>
 *
 * @author Illusion
 */
public interface PastedArea extends TemplatedArea {

    /**
     * Unloads the area, and returns a future which will be completed when the area is unloaded.
     * The future may be completed immediately, and the area will be unloaded in the main thread (WorldEdit).
     * @return A future which will be completed when the area is unloaded
     */
    CompletableFuture<Void> unload();

    /**
     * Obtains the location of where the area was pasted.
     * @return The location of the area
     */
    Location getPasteLocation();

}
