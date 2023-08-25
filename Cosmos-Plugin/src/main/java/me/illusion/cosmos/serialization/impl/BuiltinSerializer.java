package me.illusion.cosmos.serialization.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.impl.builtin.StructureTemplatedArea;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.Vector;

public class BuiltinSerializer implements CosmosSerializer {

    private static final Vector EMPTY_OFFSET = new Vector(0, 1000, 0); // Add this to min, max to end with an "empty" area (above build limit)

    @Override
    public CompletableFuture<byte[]> serialize(TemplatedArea area) {
        if (!(area instanceof StructureTemplatedArea templatedArea)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Area is not a schematic templated area"));
        }

        return CompletableFuture.supplyAsync(() -> encode(templatedArea));
    }

    @Override
    public CompletableFuture<TemplatedArea> deserialize(byte[] data) {
        return CompletableFuture.supplyAsync(() -> decode(data));
    }

    @Override
    public CompletableFuture<TemplatedArea> createArea(Cuboid bounds, Location anchor) {
        StructureManager structureManager = Bukkit.getStructureManager();

        Structure structure = structureManager.createStructure();
        Structure emptyStructure = structureManager.createStructure();

        Location min = bounds.getMin().toLocation(anchor.getWorld());
        Location max = bounds.getMax().toLocation(anchor.getWorld());

        structure.fill(min, max, true);
        emptyStructure.fill(min.clone().add(EMPTY_OFFSET), max.clone().add(EMPTY_OFFSET), false);

        return CompletableFuture.completedFuture(new StructureTemplatedArea(this, structure, emptyStructure, anchor.toVector()));
    }

    @Override
    public CompletableFuture<TemplatedArea> tryImport(File file) {
        return CompletableFuture.completedFuture(null); // This isn't hooking into any other plugins
    }

    @Override
    public String getName() {
        return "builtin";
    }

    private byte[] encode(StructureTemplatedArea area) {
        Structure main = area.getStructure();
        Structure empty = area.getEmptyStructure();

        Vector anchor = area.getAnchor();

        byte[] mainData = saveStructure(main);
        byte[] emptyData = saveStructure(empty);

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            stream.write(mainData.length);
            stream.write(mainData);
            stream.write(emptyData.length);
            stream.write(emptyData);

            stream.write(anchor.getBlockX());
            stream.write(anchor.getBlockY());
            stream.write(anchor.getBlockZ());

            return stream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] saveStructure(Structure structure) {
        StructureManager structureManager = Bukkit.getStructureManager();

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            structureManager.saveStructure(stream, structure);
            return stream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Structure readStructure(byte[] data) {
        StructureManager structureManager = Bukkit.getStructureManager();

        try (ByteArrayInputStream stream = new ByteArrayInputStream(data)) {
            return structureManager.loadStructure(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private StructureTemplatedArea decode(byte[] data) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(data)) {
            int mainLength = stream.read();
            byte[] mainData = new byte[mainLength];

            stream.read(mainData);

            int emptyLength = stream.read();
            byte[] emptyData = new byte[emptyLength];

            stream.read(emptyData);

            Structure main = readStructure(mainData);
            Structure empty = readStructure(emptyData);

            Vector anchor = new Vector(stream.read(), stream.read(), stream.read());

            return new StructureTemplatedArea(this, main, empty, anchor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
