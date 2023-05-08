package me.illusion.cosmos.command;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import io.reactivex.rxjava3.core.Completable;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.template.TemplatedArea;
import me.illusion.cosmos.utilities.command.command.impl.AdvancedCommand;
import me.illusion.cosmos.utilities.command.command.impl.ExecutionContext;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import me.illusion.cosmos.utilities.text.Placeholder;
import me.illusion.cosmos.utilities.text.TextUtils;
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

        CosmosDataContainer finalDataContainer = dataContainer;
        dataContainer.fetchTemplate(template).thenAccept(templateToDelete -> {
            if (templateToDelete == null) {
                bukkitPlayer.sendMessage("Invalid template!");
                return;
            }
            finalDataContainer.deleteTemplate(template);
        });
        //TODO: ADD CONFIRMATION

    }

}
