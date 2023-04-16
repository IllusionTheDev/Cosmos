package me.illusion.cosmos.template.grid.impl;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.Builder.Default;
import me.illusion.cosmos.template.PastedArea;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.grid.CosmosGrid;
import me.illusion.cosmos.utilities.geometry.Point;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@Builder
public class SingleWorldGrid implements CosmosGrid {

    private final Set<Integer> usedIndexes = Sets.newConcurrentHashSet();

    private final UUID worldId;

    @Default
    private final int distanceBetweenAreas = 1000;

    @Default
    private final int baseYLevel = 128;

    @Override
    public CompletableFuture<PastedArea> paste(TemplatedArea area) {
        // calculate the next index to use
        int index = calculateNextIndex();

        // generate the point for the index
        Point point = generatePoint(index);

        // paste the area at the point
        return area.paste(new Location(Bukkit.getWorld(worldId), point.getX() * distanceBetweenAreas, baseYLevel, point.getY() * distanceBetweenAreas));
    }


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

        // apply the offset to the x coordinate

        return new Point(x, y);
    }

    private int calculateNextIndex() {
        // iterate through all indexes until we find one that is not used
        for(int index = 0; index < Integer.MAX_VALUE; index++) {
            if(!usedIndexes.contains(index))
                return index;
        }

        // if we reach this point, we have no more indexes to use
        throw new IllegalStateException("No more indexes to use!");
    }
}
