package me.illusion.cosmos.template.impl;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;

public class SchematicTemplatedArea implements TemplatedArea {

    private final Clipboard clipboard;

    public SchematicTemplatedArea(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    @Override
    public CompletableFuture<PastedArea> paste(Location location) {
        World worldEditWorld = new BukkitWorld(location.getWorld());

        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(worldEditWorld, -1)) {
            new ClipboardHolder(clipboard)
                .createPaste(session)
                .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                .ignoreAirBlocks(false)
                .build();
        }

        return CompletableFuture.completedFuture(new SchematicPastedArea(location));
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

    public Clipboard getClipboard() {
        return clipboard;
    }

    private class SchematicPastedArea implements PastedArea {

        private final Location pasteLocation;

        public SchematicPastedArea(Location pasteLocation) {
            this.pasteLocation = pasteLocation;
        }

        @Override
        public CompletableFuture<Void> unload() {
            World worldEditWorld = new BukkitWorld(pasteLocation.getWorld());

            Cuboid dimensions = getDimensions();
            CuboidRegion cuboidRegion = new CuboidRegion(
                worldEditWorld,
                BlockVector3.at(dimensions.getMinX(), dimensions.getMinY(), dimensions.getMinZ()),
                BlockVector3.at(dimensions.getMaxX(), dimensions.getMaxY(), dimensions.getMaxZ())
            );

            BlockState air = BukkitAdapter.adapt(Material.AIR.createBlockData());

            try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(worldEditWorld, -1)) {
                session.setBlocks(cuboidRegion, air);
                session.commit();
            } catch (MaxChangedBlocksException e) {
                throw new RuntimeException(e);
            }

            return CompletableFuture.completedFuture(null);
        }

        @Override
        public Location getPasteLocation() {
            return pasteLocation;
        }

        @Override
        public CompletableFuture<PastedArea> paste(Location location) {
            return SchematicTemplatedArea.this.paste(location);
        }

        @Override
        public Cuboid getDimensions() {
            return SchematicTemplatedArea.this.getDimensions();
        }
    }
}
