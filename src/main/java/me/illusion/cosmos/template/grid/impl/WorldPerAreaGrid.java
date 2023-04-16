package me.illusion.cosmos.template.grid.impl;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.grid.CosmosGrid;
import me.illusion.cosmos.world.VoidGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.Vector;

public class WorldPerAreaGrid implements CosmosGrid {

    private final Map<UUID, Boolean> worldPool = new ConcurrentHashMap<>();

    private final ChunkGenerator chunkGenerator;
    private final Vector spawnLocation;

    public WorldPerAreaGrid(ChunkGenerator chunkGenerator, Vector spawnLocation) {
        this.chunkGenerator = chunkGenerator;
        this.spawnLocation = spawnLocation;
    }

    public WorldPerAreaGrid(Vector spawnLocation) {
        this(new VoidGenerator(), spawnLocation);
    }

    public WorldPerAreaGrid() {
        this(new Vector(0, 128, 0));
    }

    @Override
    public CompletableFuture<PastedArea> paste(TemplatedArea area) {
        return area.paste(new Location(Bukkit.getWorld(createWorld()), spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ()));
    }


    private UUID createWorld() {
        UUID randomId = UUID.randomUUID();

        WorldCreator creator = new WorldCreator(randomId.toString());
        creator.generator(chunkGenerator);
        creator.generateStructures(false);
        World created = creator.createWorld();

        if(created == null)
            throw new IllegalStateException("Failed to create world for area paste");

        created.setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());
        UUID worldId = created.getUID();
        worldPool.put(worldId, true);

        return worldId;
    }


}
