package me.illusion.cosmos.template.impl;


import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.world.SlimeChunk;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import me.illusion.cosmos.utilities.geometry.Point;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class SlimeTemplatedArea implements TemplatedArea {

    private final SlimePlugin slimePlugin;
    private final SlimeWorld slimeWorld;

    public SlimeTemplatedArea(SlimePlugin slimePlugin, SlimeWorld slimeWorld) {
        this.slimePlugin = slimePlugin;
        this.slimeWorld = slimeWorld;
    }

    @Override
    public CompletableFuture<PastedArea> paste(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            SlimeWorld newWorld = slimeWorld.clone(location.getWorld().getName());
            return new SlimePastedAraa(newWorld);
        });
    }

    @Override
    public Cuboid getDimensions() {
        int minSection = Integer.MIN_VALUE;
        int maxSection = Integer.MAX_VALUE;
        int minChunkX = Integer.MAX_VALUE;
        int minChunkZ = Integer.MAX_VALUE;
        int maxChunkX = Integer.MIN_VALUE;
        int maxChunkZ = Integer.MIN_VALUE;

        Map<Long, SlimeChunk> chunks = slimeWorld.getChunks();

        for (long key : chunks.keySet()) {
            Point chunkPoint = getChunkPoint(key);
            minChunkX = Math.min(minChunkX, chunkPoint.getX());
            minChunkZ = Math.min(minChunkZ, chunkPoint.getY());
            maxChunkX = Math.max(maxChunkX, chunkPoint.getX());
            maxChunkZ = Math.max(maxChunkZ, chunkPoint.getY());
        }

        Vector minPoint = getMinPoint(new Point(minChunkX, minChunkZ), minSection);
        Vector maxPoint = getMaxPoint(new Point(maxChunkX, maxChunkZ), maxSection);

        return new Cuboid(minPoint, maxPoint);
    }

    public SlimeWorld getSlimeWorld() {
        return slimeWorld;
    }

    public SlimePlugin getSlimePlugin() {
        return slimePlugin;
    }

    @Override
    public CosmosSerializer getSerializer() {
        return null;
    }

    private Point getChunkPoint(long key) {
        return new Point((int) (key >> 32), (int) key);
    }

    private Vector getMinPoint(Point chunkPoint, int minSection) {
        return new Vector((chunkPoint.getX() << 4), minSection * 16, (chunkPoint.getY() << 4));
    }

    private Vector getMaxPoint(Point chunkPoint, int maxSection) {
        return new Vector((chunkPoint.getX() << 4) + 15, maxSection * 16 + 15, (chunkPoint.getY() << 4) + 15);
    }

    private class SlimePastedAraa implements PastedArea {

        private final SlimeWorld world;

        public SlimePastedAraa(SlimeWorld world) {
            this.world = world;
        }

        @Override
        public CompletableFuture<Void> unload() {
            return CompletableFuture.runAsync(() -> {
                try {
                    world.getLoader().deleteWorld(world.getName());
                } catch (UnknownWorldException | IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public Location getPasteLocation() {
            int startX = slimeWorld.getPropertyMap().getValue(SlimeProperties.SPAWN_X);
            int startY = slimeWorld.getPropertyMap().getValue(SlimeProperties.SPAWN_Y);
            int startZ = slimeWorld.getPropertyMap().getValue(SlimeProperties.SPAWN_Z);
            World world = Bukkit.getWorld(slimeWorld.getName());

            return new Location(world, startX, startY, startZ);
        }

        @Override
        public CompletableFuture<PastedArea> paste(Location location) {
            return SlimeTemplatedArea.this.paste(location);
        }

        @Override
        public Cuboid getDimensions() {
            return SlimeTemplatedArea.this.getDimensions();
        }

        @Override
        public CosmosSerializer getSerializer() {
            return SlimeTemplatedArea.this.getSerializer();
        }
    }
}
