package me.illusion.cosmos.session;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Data;
import lombok.Getter;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.template.PastedArea;
import org.bukkit.Location;


/**
 * A simple session implementation, that uses a UUID identifier and a pasted area. This class is thread-safe.
 *
 * @author Illusion
 * @see PastedArea
 */
@Getter
@Data
public class CosmosSession {

    private final UUID uuid;
    private final PastedArea pastedArea;

    /**
     * Unloads the session.
     *
     * @return A future which will complete when the session is unloaded
     */
    public CompletableFuture<Void> unload() {
        System.out.println("Unloading session " + uuid.toString());
        return pastedArea.unload();
    }

    /**
     * Saves the session to the specified container.
     *
     * @param container The container to save the session to
     * @return A future which will complete when the session is saved
     */
    public CompletableFuture<Void> save(CosmosDataContainer container) {
        System.out.println("Saving session " + uuid.toString());
        return container.saveTemplate(uuid.toString(), pastedArea);
    }

    /**
     * Checks if the session contains the specified location.
     *
     * @param location The location to check
     * @return Whether the session contains the location
     */
    public boolean containsLocation(Location location) {
        return pastedArea.containsLocation(location);
    }

}