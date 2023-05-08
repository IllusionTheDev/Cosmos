package me.illusion.cosmos.command;

import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.utilities.command.command.impl.AdvancedCommand;
import me.illusion.cosmos.utilities.command.command.impl.ExecutionContext;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmosTemplatePasteCommand extends AdvancedCommand {

    private final CosmosPlugin plugin;
    private final MessagesFile messages;

    public CosmosTemplatePasteCommand(CosmosPlugin plugin) {
        super("cosmos templates paste <template> <container>");

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
        String container = context.getParameter("container");
        String template = context.getParameter("template");

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

        dataContainer.fetchTemplate(template).thenAccept(templateToPaste -> {
            if (templateToPaste == null) {
                bukkitPlayer.sendMessage("Invalid template!");
                return;
            }
            bukkitPlayer.sendMessage("Pasting template...");
            templateToPaste.paste(bukkitPlayer.getLocation());

        });
        bukkitPlayer.sendMessage("Template pasted!");
    }
}
