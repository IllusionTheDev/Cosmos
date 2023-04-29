package me.illusion.example.cosmosexampleplugin.listener;

import java.util.UUID;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.example.cosmosexampleplugin.CosmosExamplePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final CosmosExamplePlugin examplePlugin;

    public PlayerJoinListener(CosmosExamplePlugin examplePlugin) {
        this.examplePlugin = examplePlugin;
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        TemplatedArea template = examplePlugin.getCosmosPlugin().getTemplateCache().get("skyblock");

        if(template == null) {
            // If the server hasn't been set up, let's not attempt to load islands
            return;
        }

        Player player = event.getPlayer();
        UUID sessionId = player.getUniqueId(); // Since this is a sample plugin, we'll just make the island id the player's uuid

        examplePlugin.getSessionHolder().loadOrCreateSession(sessionId, template).thenAccept((session) -> {
            // We'll teleport the player to the island's spawn point
            player.teleport(session.getPastedArea().getPasteLocation());
        });
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        UUID sessionId = event.getPlayer().getUniqueId();

        examplePlugin.getSessionHolder().unloadSession(sessionId, true); // When the player leaves, we'll unload their island
    }
}
