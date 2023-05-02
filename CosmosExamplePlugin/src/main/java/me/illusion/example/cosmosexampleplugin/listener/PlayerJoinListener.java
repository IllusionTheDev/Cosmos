package me.illusion.example.cosmosexampleplugin.listener;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.concurrency.MainThreadExecutor;
import me.illusion.cosmos.utilities.time.Time;
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

        if (template == null) {
            // If the server hasn't been set up, let's not attempt to load islands
            return;
        }

        Player player = event.getPlayer();
        UUID sessionId = player.getUniqueId(); // Since this is a sample plugin, we'll just make the island id the player's uuid

        // Let's attempt to load or create a session. Given we automatically unload sessions after 30 minutes, if the session is still valid it'll be instantly loaded.
        // Attempting to load or create a session with an automatic unload in the background will cause the unload to be cancelled, and the session to be considered active.
        examplePlugin.getSessionHolder().loadOrCreateSession(sessionId, template).thenAcceptAsync((session) -> {
            System.out.println("Loaded session " + sessionId);

            // We'll teleport the player to the island's spawn point
            player.teleport(session.getPastedArea().getPasteLocation());
        }, MainThreadExecutor.INSTANCE);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        UUID sessionId = event.getPlayer().getUniqueId();

        // Unload the session after 30 minutes of inactivity, this should be enough time for the player to rejoin in case of a crash.
        examplePlugin.getSessionHolder().unloadAutomaticallyIn(new Time(30, TimeUnit.SECONDS), sessionId, true);
    }
}
