package me.illusion.cosmos.template.impl;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.event.CosmosPasteAreaEvent;
import me.illusion.cosmos.event.CosmosUnloadAreaEvent;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.utilities.concurrency.MainThreadExecutor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * A schematic pasted area is an area that has already been pasted, and can be unloaded.
 */
public class SchematicPastedArea extends SchematicTemplatedArea implements PastedArea {

    private final Location pasteLocation;

    public SchematicPastedArea(SchematicTemplatedArea original, Location pasteLocation) {
        super(original.getSerializer(), original.getClipboard());

        this.pasteLocation = pasteLocation;
        Bukkit.getPluginManager().callEvent(new CosmosPasteAreaEvent(this));
    }

    @Override
    public CompletableFuture<Void> unload() {
        if (!Bukkit.isPrimaryThread()) {
            System.out.println("Detected async unload. Unloading sync.");
            return CompletableFuture.runAsync(this::unload, MainThreadExecutor.INSTANCE);
        }

        System.out.println("Unloading at " + pasteLocation);

        World worldEditWorld = new BukkitWorld(pasteLocation.getWorld());
        CuboidRegion cuboidRegion = getCuboidRegion();

        BlockState air = BukkitAdapter.adapt(Material.AIR.createBlockData());

        try (EditSession session = WorldEdit.getInstance().newEditSession(worldEditWorld)) {
            session.setBlocks(cuboidRegion, air);
            session.commit();
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }

        Bukkit.getPluginManager().callEvent(new CosmosUnloadAreaEvent(this));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Location getPasteLocation() {
        return pasteLocation;
    }

    @Override
    public Clipboard getClipboard() {
        System.out.println("Re-creating clipboard for " + pasteLocation);

        CuboidRegion cuboidRegion = getCuboidRegion();
        // Given this is a pasted area, we'll need to re-make the clipboard, as the old one is no longer valid.

        World worldEditWorld = new BukkitWorld(pasteLocation.getWorld());
        BlockArrayClipboard clipboard = new BlockArrayClipboard(cuboidRegion);
        Location anchor = getPasteLocation().clone();

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
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        System.out.println("Created clipboard for " + pasteLocation);
        return clipboard;
    }

    private CuboidRegion getCuboidRegion() {
        return super.getClipboard().getRegion().getBoundingBox();
    }
}