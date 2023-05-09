package me.illusion.cosmos.command;

import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.utilities.command.command.impl.AdvancedCommand;
import me.illusion.cosmos.utilities.command.command.impl.ExecutionContext;
import me.illusion.cosmos.utilities.geometry.Cuboid;
import me.illusion.cosmos.utilities.hook.WorldEditUtils;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmosTemplateSetCommand extends AdvancedCommand {

    private final CosmosPlugin plugin;
    private final MessagesFile messages;

    public CosmosTemplateSetCommand(CosmosPlugin plugin) {
        super("cosmos templates set <template> <container>");

        this.plugin = plugin;
        this.messages = plugin.getMessages();

        addInputValidation("template", sender -> messages.sendMessage(sender, "settemplate.invalid-template-arg"));
        addInputValidation("container", sender -> messages.sendMessage(sender, "settemplate.invalid-container-arg"));
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    public void execute(CommandSender sender, ExecutionContext context) {
        Player bukkitPlayer = (Player) sender;

        String templateName = context.getParameter("template");
        String container = context.getParameter("container");

        Cuboid selection = WorldEditUtils.getPlayerSelection(bukkitPlayer);

        if (selection == null) {
            //bukkitPlayer.sendMessage("You must make a WorldEdit selection first!");
            plugin.getMessages().sendMessage(sender, "templates.set.no-selection");
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
