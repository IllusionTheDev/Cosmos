package me.illusion.cosmos.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.grid.CosmosGrid;

public class CosmosSessionHolder {

    private final Map<UUID, CosmosSession> sessions = new ConcurrentHashMap<>();

    private final CosmosGrid grid;
    private final CosmosDataContainer saveContainer;

    public CosmosSessionHolder(CosmosDataContainer container, CosmosGrid grid) {
        this.grid = grid;
        this.saveContainer = container;
    }

    public CompletableFuture<CosmosSession> createSession(UUID sessionId, TemplatedArea template) {
        return grid.paste(template).thenApply((pastedArea) -> {
            CosmosSession session = new CosmosSession(sessionId, pastedArea);
            sessions.put(sessionId, session);
            return session;
        });
    }

    public CompletableFuture<CosmosSession> loadSession(UUID sessionId) {
        return saveContainer.fetchTemplate(sessionId.toString()).thenCompose((template) -> {
            if(template == null) {
                return CompletableFuture.completedFuture(null);
            }

            return createSession(sessionId, template);
        });
    }

    public CompletableFuture<CosmosSession> loadOrCreateSession(UUID sessionId, TemplatedArea templatedArea) {
        return loadSession(sessionId).thenCompose((session) -> {
            if(session != null) {
                return CompletableFuture.completedFuture(session);
            }

            return createSession(sessionId, templatedArea);
        });
    }

    public CompletableFuture<Void> unloadSession(UUID sessionId, boolean save) {
        CosmosSession session = sessions.remove(sessionId);

        if(session == null) {
            return CompletableFuture.completedFuture(null);
        }

        if(save) {
            return session.save(saveContainer).thenCompose((v) -> session.unload());
        }

        return session.unload();
    }

    public CosmosSession getSession(UUID sessionId) {
        return sessions.get(sessionId);
    }

    public CompletableFuture<Void> unloadAll() {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for(CosmosSession session : sessions.values()) {
            futures.add(session.unload());
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
