package me.illusion.cosmos.event;

import lombok.Getter;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.event.base.CosmosEvent;
import me.illusion.cosmos.template.TemplatedArea;
import org.bukkit.event.HandlerList;

@Getter
public class CosmosTemplateMigrateEvent extends CosmosEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CosmosDataContainer sourceContainer;
    private final CosmosDataContainer targetContainer;
    private final TemplatedArea template;
    private final String templateId;

    public CosmosTemplateMigrateEvent(CosmosDataContainer sourceContainer, CosmosDataContainer targetContainer, TemplatedArea template, String templateId) {
        this.sourceContainer = sourceContainer;
        this.targetContainer = targetContainer;
        this.template = template;
        this.templateId = templateId;
    }

    public static HandlerList getHandlerList() { return HANDLER_LIST; }
    @Override
    public HandlerList getHandlers() { return HANDLER_LIST; }
}
