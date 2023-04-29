package me.illusion.example.cosmosexampleplugin;

import lombok.Getter;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.session.CosmosSessionHolder;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.template.grid.CosmosGrid;
import me.illusion.cosmos.template.grid.impl.WorldPerAreaGrid;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import me.illusion.cosmos.utilities.hook.WorldEditUtils;
import me.illusion.example.cosmosexampleplugin.command.SetTemplateCommand;
import me.illusion.example.cosmosexampleplugin.listener.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
        CosmosGrid grid = WorldPerAreaGrid.builder().maxActiveWorlds(25).build();
        cosmosPlugin.getGridRegistry().register(grid);

        sessionHolder = new CosmosSessionHolder(cosmosPlugin.getContainerRegistry().getDefaultContainer(), grid); // Save on the default container, and use the grid we just registered

        // Attempt to fetch the template from the database and cache it, if it isn't found, it simply won't cache.
        cosmosPlugin.getTemplateCache().register("skyblock", cosmosPlugin.getContainerRegistry().getDefaultContainer().fetchTemplate("skyblock"));
    }

    @Override
    public void onDisable() {
        sessionHolder.unloadAll().join();
    }
}
