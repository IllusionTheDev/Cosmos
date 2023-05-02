package me.illusion.example.cosmosexampleplugin;

import lombok.Getter;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.session.CosmosSessionHolder;
import me.illusion.cosmos.template.grid.CosmosGrid;
import me.illusion.cosmos.template.grid.impl.WorldPerAreaGrid;
import me.illusion.example.cosmosexampleplugin.command.SetTemplateCommand;
import me.illusion.example.cosmosexampleplugin.listener.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class CosmosExamplePlugin extends JavaPlugin {

    // Let's make a basic skyblock plugin!

    private CosmosSessionHolder sessionHolder;
    private CosmosPlugin cosmosPlugin;

    @Override
    public void onEnable() {
        cosmosPlugin = (CosmosPlugin) Bukkit.getPluginManager().getPlugin("Cosmos");

        initCosmos();

        getCommand("settemplate").setExecutor(new SetTemplateCommand(this));
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    private void initCosmos() {
        // On skyblock, we'll use a world per area grid, with a maximum of 25 active worlds at a time.
        // A world per area grid will create a new world for each area, automatically loading, unloading and deleting them as needed.
        CosmosGrid grid = WorldPerAreaGrid.builder().maxActiveWorlds(25).build();

        // Register the grid
        cosmosPlugin.getGridRegistry().register(grid);

        // Save on the default container, and use the grid we just registered
        sessionHolder = new CosmosSessionHolder(this, cosmosPlugin.getContainerRegistry().getDefaultContainer(), grid);

        // Attempt to fetch the template from the database and cache it, if it isn't found, it simply won't cache.
        cosmosPlugin.getTemplateCache().register("skyblock", cosmosPlugin.getContainerRegistry().getDefaultContainer().fetchTemplate("skyblock"));
    }

    @Override
    public void onDisable() {
        // We'll unload all the worlds when the plugin is disabled. We join the completable future to ensure it's completed before the plugin is disabled.
        sessionHolder.unloadAll(true).join();
    }
}
