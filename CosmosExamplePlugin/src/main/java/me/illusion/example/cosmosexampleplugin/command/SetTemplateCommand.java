package me.illusion.example.cosmosexampleplugin.command;

import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import me.illusion.cosmos.utilities.hook.WorldEditUtils;
import me.illusion.example.cosmosexampleplugin.CosmosExamplePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetTemplateCommand implements CommandExecutor {

    private final CosmosPlugin cosmosPlugin;

    public SetTemplateCommand(CosmosExamplePlugin examplePlugin) {
        this.cosmosPlugin = examplePlugin.getCosmosPlugin();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player bukkitPlayer)) {
            sender.sendMessage("You must be a player to use this command!");
            return true;
        }

        if(args.length != 1) {
            bukkitPlayer.sendMessage("Usage: /settemplate <template>");
            return true;
        }

        String templateName = args[0];
        Cuboid selection = WorldEditUtils.getPlayerSelection(bukkitPlayer);

        if(selection == null) {
            bukkitPlayer.sendMessage("You must make a WorldEdit selection first!");
            return true;
        }

        CosmosSerializer serializer = cosmosPlugin.getSerializerRegistry().get("worldedit");
        CosmosDataContainer dataContainer = cosmosPlugin.getContainerRegistry().getDefaultContainer();

        serializer.createArea(selection, bukkitPlayer.getLocation()).thenAccept(area -> {

            dataContainer.saveTemplate(templateName, area).thenRun(() -> {
                bukkitPlayer.sendMessage("Template saved!");
            });

            cosmosPlugin.getTemplateCache().register(templateName, area);
        });

        bukkitPlayer.sendMessage("Saving template...");
        return true;
    }
}
