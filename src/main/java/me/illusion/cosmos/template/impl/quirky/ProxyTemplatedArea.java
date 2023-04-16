package me.illusion.cosmos.template.impl.quirky;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.Setter;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Location;

public class ProxyTemplatedArea implements TemplatedArea {

    private final TemplatedArea underlying;
    @Setter
    private Consumer<Location> prePasteAction;

    public ProxyTemplatedArea(TemplatedArea underlying) {
        this.underlying = underlying;
    }

    @Override
    public CompletableFuture<PastedArea> paste(Location location) {
        if (prePasteAction != null) {
            prePasteAction.accept(location);
        }

        return underlying.paste(location);
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
