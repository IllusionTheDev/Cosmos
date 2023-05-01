package me.illusion.cosmos.serialization.impl;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.impl.SchematicTemplatedArea;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Location;

public class WorldEditSerializer implements CosmosSerializer {

    @Override
    public CompletableFuture<byte[]> serialize(TemplatedArea area) {
        Clipboard clipboard = ((SchematicTemplatedArea) area).getClipboard();

        return CompletableFuture.supplyAsync(() -> {
            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                ClipboardFormat format = ClipboardFormats.findByAlias("sponge"); // We could also use the format of the clipboard, but we don't know it

                try (ClipboardWriter writer = format.getWriter(stream)) {
                    writer.write(clipboard);
                }

                return stream.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<TemplatedArea> deserialize(byte[] data) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream stream = new ByteArrayInputStream(data)) {
                ClipboardFormat format = ClipboardFormats.findByAlias("sponge");

                try (ClipboardReader reader = format.getReader(stream)) {
                    return (TemplatedArea) new SchematicTemplatedArea(this, reader.read());
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<TemplatedArea> createArea(Cuboid dimensions, Location anchor) {
        World worldEditWorld = new BukkitWorld(anchor.getWorld());

        CuboidRegion cuboidRegion = new CuboidRegion(
            worldEditWorld,
            BlockVector3.at(dimensions.getMinX(), dimensions.getMinY(), dimensions.getMinZ()),
            BlockVector3.at(dimensions.getMaxX(), dimensions.getMaxY(), dimensions.getMaxZ())
        );

        BlockArrayClipboard clipboard = new BlockArrayClipboard(cuboidRegion);

        try (EditSession session = WorldEdit.getInstance().newEditSession(worldEditWorld)) {
            clipboard.setOrigin(BlockVector3.at(anchor.getX(), anchor.getY(), anchor.getZ()));

            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                session,
                cuboidRegion,
                clipboard,
                cuboidRegion.getMinimumPoint()
            );

            forwardExtentCopy.setCopyingEntities(false);
            forwardExtentCopy.setCopyingBiomes(true);

            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }

        return CompletableFuture.completedFuture(new SchematicTemplatedArea(this, clipboard));
    }

    @Override
    public CompletableFuture<TemplatedArea> tryImport(File file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Files.readAllBytes(file.toPath());
            } catch (IOException expected) {
                return null;
            }
        }).thenCompose(data -> {
            if (data == null) {
                return CompletableFuture.completedFuture(null);
            }

            return deserialize(data);
        });
    }

    @Override
    public String getName() {
        return "worldedit";
    }
}
