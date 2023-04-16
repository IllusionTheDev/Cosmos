package me.illusion.cosmos.template;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;

public interface PastedArea extends TemplatedArea {

    CompletableFuture<Void> unload();

    Location getPasteLocation();

}
