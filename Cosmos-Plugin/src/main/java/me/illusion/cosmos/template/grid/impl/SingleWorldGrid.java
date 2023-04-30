package me.illusion.cosmos.template.grid.impl;

import com.google.common.collect.Sets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Builder.Default;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.grid.CosmosGrid;
import me.illusion.cosmos.utilities.geometry.Point;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;

/**
 * A single world grid is a grid which pastes areas in a single world, in a spiral pattern.
 * <p>
 * You can specify the distance between areas, and the base Y level, which is the Y level at which the area will be pasted.
 * <p>
 *
 * @author Illusion
 */
@Builder
public class SingleWorldGrid implements CosmosGrid {

    private final Set<Integer> usedIndexes = Sets.newConcurrentHashSet();
    private final Map<PastedArea, Integer> pasteIndexes = new ConcurrentHashMap<>();

    private final UUID worldId;
    private final ChunkGenerator chunkGenerator;

    @Default
    private final int distanceBetweenAreas = 1000;

    @Default
    private final int baseYLevel = 128;

    @Override
    public CompletableFuture<PastedArea> paste(TemplatedArea area) {
        // calculate the next index to use
        int index = calculateNextIndex();

        usedIndexes.add(index);

        // generate the point for the index
        Point point = generatePoint(index);

        // paste the area at the point
        return area.paste(new Location(Bukkit.getWorld(worldId), point.getX() * distanceBetweenAreas, baseYLevel, point.getY() * distanceBetweenAreas))
            .thenApply(pastedArea -> {
                pasteIndexes.put(pastedArea, index);
                return pastedArea;
            });
    }

    @Override
    public CompletableFuture<Void> unloadAll() {
        usedIndexes.clear();
        World world = Bukkit.getWorld(worldId);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        if (world != null) {
            String name = world.getName();
            Bukkit.unloadWorld(world, false);

            futures.add(CompletableFuture.runAsync(() -> {
                File worldFolder = new File(Bukkit.getWorldContainer(), name);
                File regionFolder = new File(worldFolder, "region");

                for (File file : regionFolder.listFiles()) {
                    file.delete();
                }
            }).thenRun(() -> {
                WorldCreator creator = new WorldCreator(name);
                creator.generator(chunkGenerator);

                Bukkit.createWorld(creator);
            }));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public void registerUnload(PastedArea area) {
        Integer boxedIndex = pasteIndexes.remove(area);

        if (boxedIndex == null) {
            return;
        }

        usedIndexes.remove(boxedIndex);
    }

    /**
     * Calculates the next Point to use, based on an index (which follows a mathematical pattern).
     *
     * @param index the current index
     * @return the target point
     */
    private Point generatePoint(int index) {
        int x;
        int y;

        // calculate the number of points in the current square
        int squareSize = (int) Math.ceil(Math.sqrt(index));

        // calculate the x and y coordinates of the center of the current square
        int centerX = squareSize / 2;
        int centerY = squareSize / 2;

        // calculate the position of the index within the current square
        int posInSquare = index - (squareSize - 1) * (squareSize - 1);

        if (posInSquare <= squareSize - 1) {
            // top row
            x = centerX + posInSquare;
            y = centerY - (squareSize - 1) / 2;
        } else if (posInSquare <= 2 * (squareSize - 1)) {
            // right column
            x = centerX + (squareSize - 1) / 2;
            y = centerY + (posInSquare - (squareSize - 1));
        } else if (posInSquare <= 3 * (squareSize - 1)) {
            // bottom row
            x = centerX - (posInSquare - 2 * (squareSize - 1));
            y = centerY + (squareSize - 1) / 2;
        } else {
            // left column
            x = centerX - (squareSize - 1) / 2;
            y = centerY - (posInSquare - 3 * (squareSize - 1));
        }

        return new Point(x, y);
    }

    /**
     * Calculates the next index to use. This method will iterate through all indexes until it finds one that is not used.
     *
     * @return the next index to use
     */
    private int calculateNextIndex() {
        // iterate through all indexes until we find one that is not used
        for (int index = 0; index < Integer.MAX_VALUE; index++) {
            if (!usedIndexes.contains(index)) {
                return index;
            }
        }

        // if we reach this point, we have no more indexes to use (which should never happen, who would keep 4 billion areas in a single world?)
        throw new IllegalStateException("No more indexes to use!");
    }
}
