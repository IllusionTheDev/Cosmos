package me.illusion.cosmos.world;

import java.util.Random;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

/**
 * This class is responsible for generating a void world, which is used for pasting areas into.
 */
public class VoidGenerator extends ChunkGenerator {

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // Do nothing
    }

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {

    }

    @Override
    public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {

    }

    @Override
    public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {

    }

}
