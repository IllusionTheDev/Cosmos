package me.illusion.cosmos.command;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import io.reactivex.rxjava3.core.Completable;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.utilities.command.command.impl.AdvancedCommand;
import me.illusion.cosmos.utilities.command.command.impl.ExecutionContext;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import me.illusion.cosmos.utilities.text.Placeholder;
import me.illusion.cosmos.utilities.text.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmosTemplateListCommand extends AdvancedCommand {

    private final CosmosPlugin plugin;
    private final MessagesFile messages;

    public CosmosTemplateListCommand(CosmosPlugin plugin) {
        super("cosmos templates list <container>");

        this.plugin = plugin;
        this.messages = plugin.getMessages();

//        addInputValidation("serializer", sender -> messages.sendMessage(sender, "import.invalid-serializer-arg"));
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

        CompletableFuture<Collection<String>> templatesToPrint = dataContainer.fetchAllTemplates();

        bukkitPlayer.sendMessage("Templates in " + dataContainer.getName() + ":");
        templatesToPrint.thenAccept(templates -> {
            if (templates.isEmpty()) {
                bukkitPlayer.sendMessage("No templates found! :(");
                return;
            }

            for (String template : templates) {
                bukkitPlayer.sendMessage(" - " + template + "\n");
            }
        });

    }

}
