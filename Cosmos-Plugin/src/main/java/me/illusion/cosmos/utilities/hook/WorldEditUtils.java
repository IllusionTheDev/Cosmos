package me.illusion.cosmos.utilities.hook;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.SessionManager;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * This class is used to interact with WorldEdit, and provides some useful methods.
 *
 * @author Illusion
 */
public final class WorldEditUtils {

    private WorldEditUtils() {

    }

    /**
     * Gets the player's current selection.
     * @param player The player to get the selection of
     * @return The player's current selection
     */
    public static Cuboid getPlayerSelection(Player player) {
        WorldEdit worldEdit = WorldEdit.getInstance();
        com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(player);

        SessionManager sessionManager = worldEdit.getSessionManager();

        if (!sessionManager.contains(worldEditPlayer)) {
            return null; // The player has no session, missing permissions perhaps?
        }

        try {
            com.sk89q.worldedit.regions.Region region = sessionManager.get(worldEditPlayer).getSelection();

            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();

            return new Cuboid(asBukkitVector(min), asBukkitVector(max));
        } catch (IncompleteRegionException expected) {
            return null;
        }
    }

    /**
     * Converts a WorldEdit vector to a Bukkit vector.
     *
     * @param vector The WorldEdit vector to convert
     * @return The Bukkit vector
     */
    public static Vector asBukkitVector(BlockVector3 vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Converts a Bukkit vector to a WorldEdit vector.
     *
     * @param vector The Bukkit vector to convert
     * @return The WorldEdit vector
     */
    public static BlockVector3 asWorldEditVector(Vector vector) {
        return BlockVector3.at(vector.getX(), vector.getY(), vector.getZ());
    }

}
