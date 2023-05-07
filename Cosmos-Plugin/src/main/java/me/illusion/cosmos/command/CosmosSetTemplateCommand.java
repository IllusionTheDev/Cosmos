package me.illusion.cosmos.command;

import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.utilities.command.command.impl.AdvancedCommand;
import me.illusion.cosmos.utilities.command.command.impl.ExecutionContext;
import me.illusion.cosmos.utilities.command.language.type.Parameter;
import me.illusion.cosmos.utilities.command.language.type.ParameterTypes;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import me.illusion.cosmos.utilities.hook.WorldEditUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmosSetTemplateCommand extends AdvancedCommand {

    private final CosmosPlugin plugin;

    public CosmosSetTemplateCommand(CosmosPlugin plugin) {
        super("cosmos settemplate <template> <container>");

        this.plugin = plugin;

        registerParameter(new Parameter<>("template", ParameterTypes.STRING, true));
        registerParameter(new Parameter<>("container", ParameterTypes.STRING, true));
    }

    @Override
    public void execute(CommandSender sender, ExecutionContext context) {
        if (!(sender instanceof Player bukkitPlayer)) {
            sender.sendMessage("You must be a player to use this command!");
            return;
        }

        String templateName = context.getParameter("template");
        String container = context.getParameter("container");

        Cuboid selection = WorldEditUtils.getPlayerSelection(bukkitPlayer);

        if (selection == null) {
            bukkitPlayer.sendMessage("You must make a WorldEdit selection first!");
            return;
        }

        CosmosSerializer serializer = plugin.getSerializerRegistry().get("worldedit");
        CosmosDataContainer dataContainer = null;

        if (container != null) {
            dataContainer = plugin.getContainerRegistry().getContainer(container);

            if (dataContainer == null) {
                bukkitPlayer.sendMessage("Invalid container!");
                return;
            }
        } else {
            dataContainer = plugin.getContainerRegistry().getDefaultContainer();
        }

        CosmosDataContainer finalDataContainer = dataContainer;
        serializer.createArea(selection, bukkitPlayer.getLocation()).thenAccept(area -> {

            finalDataContainer.saveTemplate(templateName, area).thenRun(() -> {
                bukkitPlayer.sendMessage("Template saved!");
            });

            plugin.getTemplateCache().register(templateName, area);
        });

        bukkitPlayer.sendMessage("Saving template...");
    }
}
