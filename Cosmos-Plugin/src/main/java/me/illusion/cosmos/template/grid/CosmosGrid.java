package me.illusion.cosmos.template.grid;

import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;

/**
 * A grid is an object responsible for calculating the location of a pasted area.
 * <p>
 *
 * @author Illusion
 */
public interface CosmosGrid {

    /**
     * Calculates the location of the area, and pastes it.
     *
     * @param area The area to paste
     * @return A future of a Pasted Area, which can be used to get the location of the area, and to undo the paste.
     */
    CompletableFuture<PastedArea> paste(TemplatedArea area);

    /**
     * Calculates the location of a future area, and eventually pastes it.
     *
     * @param areaFuture The future of the area to paste
     * @return A future of a Pasted Area, which can be used to get the location of the area, and to undo the paste.
     */
    default CompletableFuture<PastedArea> paste(CompletableFuture<TemplatedArea> areaFuture) {
        return areaFuture.thenCompose(this::paste);
    }

    /**
     * Unloads all pasted areas.
     *
     * @return A future which will be completed when all areas are unloaded.
     */
    CompletableFuture<Void> unloadAll();

    /**
     * Called when an area is unloaded. There is no guarantee that the area was pasted by this grid.
     *
     * @param area The area which was unloaded
     */
    void registerUnload(PastedArea area);

    /**
     * Called when the grid is registered.
     */
    default void init(CosmosPlugin plugin) {

    }

}
