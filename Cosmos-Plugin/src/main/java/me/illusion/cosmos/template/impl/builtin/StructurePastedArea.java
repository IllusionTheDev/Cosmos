package me.illusion.cosmos.template.impl.builtin;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Location;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;

public class StructurePastedArea implements PastedArea {

    private final Location pasteLocation;
    private final StructureTemplatedArea templatedArea;

    public StructurePastedArea(Location pasteLocation, StructureTemplatedArea templatedArea) {
        this.pasteLocation = pasteLocation;
        this.templatedArea = templatedArea;
    }

    @Override
    public CompletableFuture<Void> unload() {
        Structure empty = templatedArea.getEmptyStructure();
        Location relative = pasteLocation.clone().subtract(templatedArea.getAnchor());
        Random random = ThreadLocalRandom.current();

        empty.place(relative, true, StructureRotation.NONE, Mirror.NONE, 0, 1, random);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Location getPasteLocation() {
        return pasteLocation;
    }

    @Override
    public CompletableFuture<PastedArea> paste(Location location) {
        return templatedArea.paste(location);
    }

    @Override
    public Cuboid getDimensions() {
        return templatedArea.getDimensions();
    }

    @Override
    public CosmosSerializer getSerializer() {
        return templatedArea.getSerializer();
    }
}
