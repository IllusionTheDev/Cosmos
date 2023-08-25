package me.illusion.cosmos.template.impl.builtin;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Location;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;
import org.bukkit.util.Vector;

public class StructureTemplatedArea implements TemplatedArea {

    private final CosmosSerializer serializer;

    private final Structure structure;
    private final Structure emptyStructure;
    private final Vector anchor;

    public StructureTemplatedArea(CosmosSerializer serializer, Structure structure, Structure emptyStructure, Vector anchor) {
        this.serializer = serializer;
        this.structure = structure;
        this.emptyStructure = emptyStructure;
        this.anchor = anchor;
    }

    @Override
    public CompletableFuture<PastedArea> paste(Location location) {
        Random random = ThreadLocalRandom.current(); // This shouldn't be used if I pass 1 as integrity

        Location relative = location.clone().subtract(anchor);
        structure.place(relative, true, StructureRotation.NONE, Mirror.NONE, 0, 1, random);

        return CompletableFuture.completedFuture(new StructurePastedArea(location, this));
    }

    @Override
    public Cuboid getDimensions() {
        Vector size = structure.getSize();

        // Anchor is an offset to the minimum point
        Vector minimum = anchor.clone().multiply(-1);
        Vector maximum = minimum.clone().add(size);

        return new Cuboid(minimum, maximum);
    }

    @Override
    public CosmosSerializer getSerializer() {
        return serializer;
    }

    public Vector getAnchor() {
        return anchor;
    }

    public Structure getEmptyStructure() {
        return emptyStructure;
    }

    public Structure getStructure() {
        return structure;
    }
}
