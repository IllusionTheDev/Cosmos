package me.illusion.cosmos.template.impl.quirky;

import java.util.concurrent.CompletableFuture;
import lombok.Setter;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Location;

/**
 * This is a proxy for a pasted area, which allows you to run code before and after the area is unloaded.
 * This is meant for internal use, for things like detecting unloads to unregister areas from a grid.
 */
public class ProxyPastedArea implements PastedArea {

    private final PastedArea underlying;

    public ProxyPastedArea(PastedArea underlying) {
        this.underlying = underlying;
    }

    @Setter
    private Runnable preUnloadAction;

    @Setter
    private Runnable postUnloadAction;

    @Setter
    private Runnable prePasteAction;

    @Setter
    private Runnable postPasteAction;

    @Override
    public CompletableFuture<Void> unload() {
        if(preUnloadAction != null)
            preUnloadAction.run();

        CompletableFuture<Void> future = underlying.unload();

        future.thenRun(() -> {
            if(postUnloadAction != null)
                postUnloadAction.run();
        });

        return future;
    }

    @Override
    public Location getPasteLocation() {
        return underlying.getPasteLocation();
    }

    @Override
    public CompletableFuture<PastedArea> paste(Location location) {
        if(prePasteAction != null)
            prePasteAction.run();

        CompletableFuture<PastedArea> future = underlying.paste(location);

        future.thenRun(() -> {
            if(postPasteAction != null)
                postPasteAction.run();
        });

        return future;
    }

    @Override
    public Cuboid getDimensions() {
        return underlying.getDimensions();
    }

    @Override
    public CosmosSerializer getSerializer() {
        return underlying.getSerializer();
    }
}
