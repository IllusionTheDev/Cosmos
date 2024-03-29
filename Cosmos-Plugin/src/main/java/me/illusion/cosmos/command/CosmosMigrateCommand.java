package me.illusion.cosmos.command;

import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.event.CosmosTemplateMigrateEvent;
import me.illusion.cosmos.utilities.command.command.impl.AdvancedCommand;
import me.illusion.cosmos.utilities.command.command.impl.ExecutionContext;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import me.illusion.cosmos.utilities.text.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CosmosMigrateCommand extends AdvancedCommand {

    private final CosmosPlugin plugin;
    private final MessagesFile messages;

    public CosmosMigrateCommand(CosmosPlugin plugin) {
        super("cosmos migrate <template> <source> <destination>");

        this.plugin = plugin;
        this.messages = plugin.getMessages();

        addInputValidation("template", sender -> messages.sendMessage(sender, "migrate.invalid-template-arg"));
        addInputValidation("source", sender -> messages.sendMessage(sender, "migrate.invalid-source-arg"));
        addInputValidation("destination", sender -> messages.sendMessage(sender, "migrate.invalid-destination-arg"));
    }

    @Override
    public void execute(CommandSender sender, ExecutionContext context) {
        String templateName = context.getParameter("template");
        String source = context.getParameter("source");
        String destination = context.getParameter("destination");

        Placeholder<CommandSender> sourcePlaceholder = new Placeholder<>("%source%", source);
        Placeholder<CommandSender> destinationPlaceholder = new Placeholder<>("%destination%", destination);
        Placeholder<CommandSender> templatePlaceholder = new Placeholder<>("%template%", templateName);

        CosmosDataContainer sourceContainer = plugin.getContainerRegistry().getContainer(source);
        CosmosDataContainer destinationContainer = plugin.getContainerRegistry().getContainer(destination);

        if (sourceContainer == null) {
            plugin.getMessages().sendMessage(sender, "migrate.invalid-source", sourcePlaceholder);
            return;
        }

        if (destinationContainer == null) {
            plugin.getMessages().sendMessage(sender, "migrate.invalid-destination", destinationPlaceholder);
            return;
        }

        if (templateName.equalsIgnoreCase("all")) {
            sourceContainer.fetchAllTemplates().thenAccept(allTemplates -> {
                List<CompletableFuture<?>> futures = new ArrayList<>();

                for (String fetchedTemplateName : allTemplates) {
                    futures.add(sourceContainer.fetchTemplate(fetchedTemplateName)
                            .thenCompose(template -> destinationContainer.saveTemplate(fetchedTemplateName, template)));
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                    plugin.getMessages().sendMessage(sender, "migrate.success", sourcePlaceholder, destinationPlaceholder);
                });
            });

            plugin.getMessages().sendMessage(sender, "migrate.started", sourcePlaceholder, destinationPlaceholder);
            return;
        }

        sourceContainer.fetchTemplate(templateName).thenCompose(template -> {
            if (template == null) {
                plugin.getMessages().sendMessage(sender, "migrate.invalid-template", templatePlaceholder);
                return CompletableFuture.completedFuture(null);
            }

            return destinationContainer.saveTemplate(templateName, template).thenRun(() -> {
                plugin.getMessages().sendMessage(sender, "migrate.success", sourcePlaceholder, destinationPlaceholder);

                Bukkit.getPluginManager().callEvent(new CosmosTemplateMigrateEvent(sourceContainer, destinationContainer, template, templateName));
            });
        });

        plugin.getMessages().sendMessage(sender, "migrate.started", sourcePlaceholder, destinationPlaceholder);
    }
}
