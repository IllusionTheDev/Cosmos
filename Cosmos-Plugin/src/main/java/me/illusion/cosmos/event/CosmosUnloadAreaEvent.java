package me.illusion.cosmos.event;

import lombok.Getter;
import me.illusion.cosmos.event.base.CosmosEvent;
import me.illusion.cosmos.template.PastedArea;
import org.bukkit.event.HandlerList;

@Getter
public class CosmosUnloadAreaEvent extends CosmosEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final PastedArea pastedArea;

    public CosmosUnloadAreaEvent(PastedArea pastedArea) {
        this.pastedArea = pastedArea;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
