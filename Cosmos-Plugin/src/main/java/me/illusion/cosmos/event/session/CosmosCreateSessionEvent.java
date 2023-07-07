package me.illusion.cosmos.event.session;

import me.illusion.cosmos.event.base.CosmosEvent;
import me.illusion.cosmos.session.CosmosSession;
import org.bukkit.event.HandlerList;

public class CosmosCreateSessionEvent extends CosmosEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CosmosSession session;

    public CosmosCreateSessionEvent(CosmosSession session) {
        this.session = session;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public CosmosSession getSession() {
        return session;
    }
}
