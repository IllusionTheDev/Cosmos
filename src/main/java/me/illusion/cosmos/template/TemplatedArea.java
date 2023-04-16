package me.illusion.cosmos.template;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Location;

public interface TemplatedArea {

    CompletableFuture<PastedArea> paste(Location location);

    Cuboid getDimensions();

}
