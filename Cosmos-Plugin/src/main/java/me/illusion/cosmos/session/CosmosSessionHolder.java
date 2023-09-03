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
import me.illusion.cosmos.event.session.CosmosCreateSessionEvent;
import me.illusion.cosmos.grid.CosmosGrid;
import me.illusion.cosmos.session.task.UnloadRequest;
import me.illusion.cosmos.session.task.UnloadTask;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.time.Time;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

    private final Map<UUID, CompletableFuture<?>> pendingUnloads = new ConcurrentHashMap<>();

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

        CompletableFuture<?> pendingUnload = pendingUnloads.get(sessionId);

        if (pendingUnload != null) {
            System.out.println("Session " + sessionId + " is pending unload. Waiting...");
            return pendingUnload.thenCompose((v) -> createSession(sessionId, template));
        }

        CosmosSession existingSession = sessions.get(sessionId);

        if (existingSession != null) {
            return CompletableFuture.completedFuture(existingSession);
        }

        System.out.println("Creating session " + sessionId.toString());
        return grid.paste(template).thenApply((pastedArea) -> {
            System.out.println("Created session " + sessionId);

            CosmosSession session = new CosmosSession(sessionId, pastedArea);
            Bukkit.getPluginManager().callEvent(new CosmosCreateSessionEvent(session));
            sessions.put(sessionId, session);
            return session;
        }).exceptionally((e) -> {
            e.printStackTrace();
            return null;
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

        CompletableFuture<?> pendingUnload = pendingUnloads.get(sessionId);

        if (pendingUnload != null) {
            System.out.println("Session " + sessionId + " is pending unload. Waiting...");
            return pendingUnload.thenCompose((v) -> loadSession(sessionId));
        }

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

        CompletableFuture<?> pendingUnload = pendingUnloads.get(sessionId);

        if (pendingUnload != null) {
            System.out.println("Session " + sessionId + " is pending unload. Waiting...");
            return pendingUnload.thenCompose((v) -> loadOrCreateSession(sessionId, templatedArea));
        }

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
     * @param save      Whether to save the session to the database
     * @param async     Whether to save the session asynchronously
     * @return A future which will complete when the session is unloaded
     */
    public CompletableFuture<Void> unloadSession(UUID sessionId, boolean save, boolean async) {
        CosmosSession session = sessions.get(sessionId);

        if (session == null) {
            return CompletableFuture.completedFuture(null);
        }

        cancelUnload(sessionId);

        if (save) {
            // We unload after everything is saved to prevent any issues with servers stopping while data is being unloaded (if it stops, unloadAll will keep running)
            return registerUnload(sessionId, session.save(saveContainer, async).thenCompose((v) -> session.unload()).thenRun(() -> sessions.remove(sessionId)));
        }

        return registerUnload(sessionId, session.unload().thenRun(() -> sessions.remove(sessionId)));
    }

    private CompletableFuture<Void> registerUnload(UUID sessionId, CompletableFuture<Void> future) {
        pendingUnloads.put(sessionId, future);
        future.thenRun(() -> pendingUnloads.remove(sessionId));
        return future;
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
    public CompletableFuture<Void> unloadAll(boolean save, boolean async) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (UUID sessionId : sessions.keySet()) {
            futures.add(unloadSession(sessionId, save, async));
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
        CosmosSession session = sessions.get(sessionId);

        if (session == null) {
            return CompletableFuture.completedFuture(false);
        }

        if (save) {
            session.save(saveContainer);
        }

        long epoch = Instant.now().getEpochSecond() + time.as(TimeUnit.SECONDS);

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        UnloadRequest request = new UnloadRequest(sessionId, epoch, future);
        UnloadTask task = new UnloadTask(plugin, request);

        future = future.thenCompose(success -> {
            unloadTasks.remove(sessionId);

            if (Boolean.TRUE.equals(success)) {
                return unloadSession(sessionId, save, true).thenApply(v -> true);
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

        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Gets the session at a location.
     *
     * @param location The location
     * @return The session, or null if it does not exist
     */
    public CosmosSession getSessionAt(Location location) {
        for (CosmosSession session : sessions.values()) {
            if (session.containsLocation(location)) {
                return session;
            }
        }

        return null;
    }

    /**
     * Gets the number of sessions in memory.
     *
     * @return The number of sessions
     */
    public int getSessionCount() {
        return sessions.size();
    }
}
