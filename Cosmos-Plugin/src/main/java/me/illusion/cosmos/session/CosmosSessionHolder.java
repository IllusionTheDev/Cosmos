package me.illusion.cosmos.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.grid.CosmosGrid;

/**
 * A simple session holder implementation, that uses a UUID identifier to store and retrieve sessions. This class is thread-safe.
 *
 * @author Illusion
 * @see CosmosSession
 */
public class CosmosSessionHolder {

    private final Map<UUID, CosmosSession> sessions = new ConcurrentHashMap<>();

    private final CosmosGrid grid;
    private final CosmosDataContainer saveContainer;

    /**
     * Creates a session holder with the specified container and grid.
     *
     * @param container The container to save sessions to
     * @param grid      The grid to paste sessions with
     */
    public CosmosSessionHolder(CosmosDataContainer container, CosmosGrid grid) {
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
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
