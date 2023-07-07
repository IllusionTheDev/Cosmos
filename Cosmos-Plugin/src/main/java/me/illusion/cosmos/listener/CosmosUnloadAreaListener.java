package me.illusion.cosmos.listener;

import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.event.area.CosmosUnloadAreaEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CosmosUnloadAreaListener implements Listener {

    private final CosmosPlugin plugin;

    public CosmosUnloadAreaListener(CosmosPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onUnload(CosmosUnloadAreaEvent event) {
        System.out.println("CosmosUnloadAreaEvent");
        plugin.getGridRegistry().handleUnload(event.getPastedArea());
    }
}
