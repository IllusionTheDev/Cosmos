package me.illusion.cosmos.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.session.task.UnloadRequest;
import me.illusion.cosmos.session.task.UnloadTask;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.grid.CosmosGrid;
import me.illusion.cosmos.utilities.time.Time;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A simple session holder implementation, that uses a UUID identifier to store and retrieve sessions. This class is thread-safe.
 *
 * @author Illusion
 * @see CosmosSession
 */
public class CosmosSessionHolder {

    private final Map<UUID, CosmosSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, UnloadTask> unloadTasks = new ConcurrentHashMap<>();

    private final JavaPlugin plugin;
    private final CosmosGrid grid;
    private final CosmosDataContainer saveContainer;

    /**
     * Creates a session holder with the specified container and grid.
     *
     * @param plugin    The plugin to run tasks with
     * @param container The container to save sessions to
     * @param grid      The grid to paste sessions with
     */
    public CosmosSessionHolder(JavaPlugin plugin, CosmosDataContainer container, CosmosGrid grid) {
        this.plugin = plugin;
        this.grid = grid;
        this.saveContainer = container;
    }

    /**
     * Creates a session with the specified UUID and template.
     *
     * @param sessionId The UUID of the session
     * @param template  The template to paste
     * @return A future which will complete with the session
     */
    public CompletableFuture<CosmosSession> createSession(UUID sessionId, TemplatedArea template) {
        cancelUnload(sessionId);

        CosmosSession existingSession = sessions.get(sessionId);

        if (existingSession != null) {
            return CompletableFuture.completedFuture(existingSession);
        }

        return grid.paste(template).thenApply((pastedArea) -> {
            CosmosSession session = new CosmosSession(sessionId, pastedArea);
            sessions.put(sessionId, session);
            return session;
        });
    }

    /**
     * Attempts to load a session from the database.
     *
     * @param sessionId The UUID of the session
     * @return A future which will complete with the session, or null if it does not exist
     */
    public CompletableFuture<CosmosSession> loadSession(UUID sessionId) {
        cancelUnload(sessionId);

        CosmosSession existingSession = sessions.get(sessionId);

        if (existingSession != null) {
            return CompletableFuture.completedFuture(existingSession);
        }

        return saveContainer.fetchTemplate(sessionId.toString()).thenCompose((template) -> {
            if (template == null) {
                return CompletableFuture.completedFuture(null);
            }

            return createSession(sessionId, template);
        });
    }

    /**
     * Attempts to load a session from the database, or creates a new one if it does not exist.
     *
     * @param sessionId     The UUID of the session
     * @param templatedArea The template to paste if the session does not exist
     * @return A future which will complete with the session
     */
    public CompletableFuture<CosmosSession> loadOrCreateSession(UUID sessionId, TemplatedArea templatedArea) {
        cancelUnload(sessionId);

        CosmosSession existingSession = sessions.get(sessionId);

        if (existingSession != null) {
            return CompletableFuture.completedFuture(existingSession);
        }

        return loadSession(sessionId).thenCompose((session) -> {
            if (session != null) {
                return CompletableFuture.completedFuture(session);
            }

            return createSession(sessionId, templatedArea);
        });
    }

    /**
     * Unloads a session from memory.
     *
     * @param sessionId The UUID of the session
     * @param save      Whether or not to save the session to the database
     * @return A future which will complete when the session is unloaded
     */
    public CompletableFuture<Void> unloadSession(UUID sessionId, boolean save) {
        CosmosSession session = sessions.get(sessionId);

        if (session == null) {
            return CompletableFuture.completedFuture(null);
        }

        cancelUnload(sessionId);

        if (save) {
            // We unload after everything is saved to prevent any issues with servers stopping while data is being unloaded (if it stops, unloadAll will keep running)
            return session.save(saveContainer).thenCompose((v) -> session.unload()).thenRun(() -> sessions.remove(sessionId));
        }

        return session.unload().thenRun(() -> sessions.remove(sessionId));
    }

    /**
     * Gets a session from memory.
     *
     * @param sessionId The UUID of the session
     * @return The session, or null if it does not exist
     */
    public CosmosSession getSession(UUID sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Unloads all sessions from memory.
     *
     * @return A future which will complete when all sessions are unloaded
     */
    public CompletableFuture<Void> unloadAll() {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (CosmosSession session : sessions.values()) {
            futures.add(session.unload());
            cancelUnload(session.getUuid());
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Attempts to unload a session after a specified amount of time.
     * If a session loads before the time is up, the future will cancel.
     * If a session unloads before the time is up, the future will complete.
     * @param time The time to wait before unloading
     * @param sessionId The UUID of the session
     * @return A future which will complete when the session is unloaded, returns true if the session was unloaded, false if it was loaded
     */
    public CompletableFuture<Boolean> unloadAutomaticallyIn(Time time, UUID sessionId, boolean save) {
        long epoch = Instant.now().getEpochSecond() + time.as(TimeUnit.SECONDS);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        UnloadTask task = new UnloadTask(plugin, new UnloadRequest(epoch, future));

        future = future.thenCompose(success -> {
            unloadTasks.remove(sessionId);

            if(success) {
                return unloadSession(sessionId, save).thenApply(v -> true);
            }

            return CompletableFuture.completedFuture(false);
        });

        unloadTasks.put(sessionId, task);
        return future;
    }

    /**
     * Cancels an unload task.
     * @param sessionId The UUID of the session
     */
    public void cancelUnload(UUID sessionId) {
        UnloadTask task = unloadTasks.remove(sessionId);

        if(task != null) {
            task.cancel();
        }
    }
}
