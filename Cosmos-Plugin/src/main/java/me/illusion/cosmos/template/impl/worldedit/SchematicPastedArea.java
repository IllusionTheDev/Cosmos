package me.illusion.cosmos.template.impl.worldedit;

import com.sk89q.worldedit.EditSession;
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
import me.illusion.cosmos.event.area.CosmosPasteAreaEvent;
import me.illusion.cosmos.event.area.CosmosUnloadAreaEvent;
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
        if (!Bukkit.isPrimaryThread() && !Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) { // FAWE is allowed to unload async
            System.out.println("Detected async unload. Unloading sync.");
            return CompletableFuture.runAsync(this::unload, MainThreadExecutor.INSTANCE);
        }

        World worldEditWorld = new BukkitWorld(pasteLocation.getWorld());
        CuboidRegion cuboidRegion = getCuboidRegion();

        BlockState air = BukkitAdapter.adapt(Material.AIR.createBlockData());

        try (EditSession session = WorldEdit.getInstance().newEditSession(worldEditWorld)) {
            session.setBlocks(cuboidRegion, air);
            Operations.complete(session.commit());
        } catch (WorldEditException e) {
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
        CuboidRegion cuboidRegion = getCuboidRegion();
        // Given this is a pasted area, we'll need to re-make the clipboard, as the old one is no longer valid.

        World worldEditWorld = new BukkitWorld(pasteLocation.getWorld());
        BlockArrayClipboard clipboard = new BlockArrayClipboard(cuboidRegion);
        Location anchor = getPasteLocation().clone();

        System.out.println("Getting clipboard between " + cuboidRegion.getMinimumPoint() + " and " + cuboidRegion.getMaximumPoint());
        System.out.println("World: " + worldEditWorld.getName());

        try (EditSession session = WorldEdit.getInstance().newEditSession(worldEditWorld)) {
            clipboard.setOrigin(BlockVector3.at(anchor.getX(), anchor.getY(), anchor.getZ()));

            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                session,
                cuboidRegion,
                clipboard,
                cuboidRegion.getMinimumPoint()
            );

            forwardExtentCopy.setCopyingEntities(true); // sure we can copy entities over
            forwardExtentCopy.setCopyingBiomes(true);

            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return clipboard;
    }

    private CuboidRegion getCuboidRegion() {
        return super.getClipboard().getRegion().getBoundingBox();
    }
}