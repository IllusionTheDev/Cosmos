package me.illusion.cosmos.event.base;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public abstract class CosmosEvent extends Event {

    public CosmosEvent() {
        super(!Bukkit.isPrimaryThread());
    }

}
