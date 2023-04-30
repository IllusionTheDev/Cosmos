package me.illusion.cosmos.template.impl;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.concurrency.MainThreadExecutor;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * A Schematic Templated Area is a templated area which is based on a WorldEdit clipboard.
 * <p>
 *
 * @author Illusion
 */
public class SchematicTemplatedArea implements TemplatedArea {

    private final CosmosSerializer serializer;
    private final Clipboard clipboard;

    public SchematicTemplatedArea(CosmosSerializer serializer, Clipboard clipboard) {
        this.serializer = serializer;
        this.clipboard = clipboard;
    }

    @Override
    public CompletableFuture<PastedArea> paste(Location location) {
        if (!Bukkit.isPrimaryThread()) {
            CompletableFuture<PastedArea> future = new CompletableFuture<>();
            CompletableFuture.runAsync(() -> paste(location).thenAccept(future::complete),
                MainThreadExecutor.INSTANCE); // paste sync and then complete the future
            return future;
        }

        System.out.println("Pasting at " + location);

        World worldEditWorld = new BukkitWorld(location.getWorld());

        try (EditSession session = WorldEdit.getInstance().newEditSession(worldEditWorld)) {
            Operation operation = new ClipboardHolder(clipboard)
                .createPaste(session)
                .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                .ignoreAirBlocks(false)
                .build();

            Operations.complete(operation);
        } catch (WorldEditException e) {
            return CompletableFuture.failedFuture(e);
        }

        return CompletableFuture.completedFuture(new SchematicPastedArea(this, location));
    }

    @Override
    public Cuboid getDimensions() {
        double minX = clipboard.getMinimumPoint().getX();
        double minY = clipboard.getMinimumPoint().getY();
        double minZ = clipboard.getMinimumPoint().getZ();

        double maxX = clipboard.getMaximumPoint().getX();
        double maxY = clipboard.getMaximumPoint().getY();
        double maxZ = clipboard.getMaximumPoint().getZ();

        return new Cuboid(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public CosmosSerializer getSerializer() {
        return serializer;
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

}
