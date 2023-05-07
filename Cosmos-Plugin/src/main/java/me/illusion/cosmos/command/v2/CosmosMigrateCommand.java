package me.illusion.cosmos.command.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.command.v2.CosmosMigrateCommand.MigrationExecution;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.event.CosmosTemplateMigrateEvent;
import me.illusion.cosmos.utilities.commandv2.language.AbstractObjectiveModel;
import me.illusion.cosmos.utilities.commandv2.language.CompiledObjective;
import me.illusion.cosmos.utilities.commandv2.language.data.ObjectiveMetadata;
import me.illusion.cosmos.utilities.commandv2.language.type.Parameter;
import me.illusion.cosmos.utilities.commandv2.language.type.ParameterTypes;
import me.illusion.cosmos.utilities.text.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CosmosMigrateCommand extends AbstractObjectiveModel<MigrationExecution> {

    private final CosmosPlugin plugin;

    public CosmosMigrateCommand(CosmosPlugin plugin) {
        super("cosmos migrate <template> <source> <destination>");

        this.plugin = plugin;

        registerParameter(new Parameter<>("template", ParameterTypes.STRING, true));
        registerParameter(new Parameter<>("source", ParameterTypes.STRING, true));
        registerParameter(new Parameter<>("destination", ParameterTypes.STRING, true));
    }

    @Override
    public MigrationExecution compile(ObjectiveMetadata metadata) {
        return new MigrationExecution(metadata);
    }

    public class MigrationExecution extends CompiledObjective {

        public MigrationExecution(ObjectiveMetadata metadata) {
            super(metadata);
        }

        @Override
        public void execute(CommandSender sender) {
            String templateName = getParameter("template");
            String source = getParameter("source");
            String destination = getParameter("destination");

            if (templateName.isEmpty()) {
                plugin.getMessages().sendMessage(sender, "migrate.invalid-template-arg");
                return;
            }

            if (source.isEmpty()) {
                plugin.getMessages().sendMessage(sender, "migrate.invalid-source-arg");
                return;
            }

            if (destination.isEmpty()) {
                plugin.getMessages().sendMessage(sender, "migrate.invalid-destination-arg");
                return;
            }

//        List<Placeholder<?>> placeholders = Arrays.asList(
//                new Placeholder<>("%template%", templateName),
//                new Placeholder<>("%source%", source),
//                new Placeholder<>("%destination%", destination)
//        );

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
}
