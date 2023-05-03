package me.illusion.cosmos.event;

import lombok.Getter;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.event.base.CosmosEvent;
import org.bukkit.event.HandlerList;

@Getter
public class CosmosDefaultContainerInitializedEvent extends CosmosEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CosmosDataContainer container;

    public CosmosDefaultContainerInitializedEvent(CosmosDataContainer container) {
        this.container = container;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
