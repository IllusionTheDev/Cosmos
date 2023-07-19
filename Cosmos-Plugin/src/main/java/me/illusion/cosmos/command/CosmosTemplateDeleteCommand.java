package me.illusion.cosmos.command;

import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.menu.generic.GenericConfirmationMenu;
import me.illusion.cosmos.utilities.command.command.impl.AdvancedCommand;
import me.illusion.cosmos.utilities.command.command.impl.ExecutionContext;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import me.illusion.cosmos.utilities.text.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmosTemplateDeleteCommand extends AdvancedCommand {

    private final CosmosPlugin plugin;
    private final MessagesFile messages;

    public CosmosTemplateDeleteCommand(CosmosPlugin plugin) {
        super("cosmos templates delete <template> <container>");

        this.plugin = plugin;
        this.messages = plugin.getMessages();

        addInputValidation("template", sender -> messages.sendMessage(sender, "templates.delete.invalid-template-arg"));
        addInputValidation("container", sender -> messages.sendMessage(sender, "templates.delete.invalid-container-arg"));
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    public void execute(CommandSender sender, ExecutionContext context) {

        if (!(sender instanceof Player player)) {
            return;
        }
        String container = context.getParameter("container");
        String template = context.getParameter("template");

        CosmosDataContainer dataContainer = null;

        if (container != null) {
            dataContainer = plugin.getContainerRegistry().getContainer(container);

            if (dataContainer == null) {
                player.sendMessage("Invalid container!");
                return;
            }
        } else {
            dataContainer = plugin.getContainerRegistry().getDefaultContainer();
        }

        Placeholder<Player> templatePlaceholder = new Placeholder<>("template", template);

        CosmosDataContainer finalDataContainer = dataContainer;

        GenericConfirmationMenu confirmationMenu = new GenericConfirmationMenu(plugin, "delete-template-confirm", player);

        confirmationMenu.onConfirm(() -> {
            finalDataContainer.fetchTemplate(template).thenAccept(templateToDelete -> {
                if (templateToDelete == null) {
                    messages.sendMessage(player, "template.delete-not-found", templatePlaceholder);
                    return;
                }
                finalDataContainer.deleteTemplate(template).thenRun(() -> {
                    messages.sendMessage(player, "template.delete-success", templatePlaceholder);
                });

            });
        });

        confirmationMenu.onDeny(() -> {
            messages.sendMessage(player, "template.delete-cancelled");
            confirmationMenu.close();
        });
    }

}
