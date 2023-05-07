package me.illusion.cosmos.command.v2;

import java.io.File;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.command.v2.CosmosImportCommand.ImportExecution;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.utilities.commandv2.language.AbstractObjectiveModel;
import me.illusion.cosmos.utilities.commandv2.language.CompiledObjective;
import me.illusion.cosmos.utilities.commandv2.language.data.ObjectiveMetadata;
import me.illusion.cosmos.utilities.commandv2.language.type.Parameter;
import me.illusion.cosmos.utilities.commandv2.language.type.ParameterTypes;
import me.illusion.cosmos.utilities.text.Placeholder;
import me.illusion.cosmos.utilities.text.TextUtils;
import org.bukkit.command.CommandSender;

public class CosmosImportCommand extends AbstractObjectiveModel<ImportExecution> {

    private final CosmosPlugin plugin;

    public CosmosImportCommand(CosmosPlugin plugin) {
        super("cosmos import <serializer> <filename>");

        this.plugin = plugin;

        registerParameter(new Parameter<>("serializer", ParameterTypes.STRING, true));
        registerParameter(new Parameter<>("filename", ParameterTypes.STRING, true));
    }

    @Override
    public ImportExecution compile(ObjectiveMetadata metadata) {
        return new ImportExecution(metadata);
    }

    public class ImportExecution extends CompiledObjective {

        public ImportExecution(ObjectiveMetadata metadata) {
            super(metadata);
        }

        @Override
        public void execute(CommandSender sender) {
            String serializer = getParameter("serializer");
            String fileName = getParameter("filename");

            if (serializer.isEmpty()) {
                plugin.getMessages().sendMessage(sender, "import.invalid-serializer-arg");
                return;
            }

            if (fileName.isEmpty()) {
                plugin.getMessages().sendMessage(sender, "import.invalid-file-arg");
                return;
            }

            CosmosSerializer cosmosSerializer = plugin.getSerializerRegistry().get(serializer);

            if (cosmosSerializer == null) {
                plugin.getMessages().sendMessage(sender, "import.invalid-serializer");
                return;
            }

            File file = new File(plugin.getDataFolder() + File.separator + "import", fileName);

            Placeholder<CommandSender> filePlaceholder = new Placeholder<>("%file%", fileName);
            Placeholder<CommandSender> serializerPlaceholder = new Placeholder<>("%serializer%", serializer);

            if (!file.exists()) {
                plugin.getMessages().sendMessage(sender, "import.invalid-file", filePlaceholder);
                return;
            }

            cosmosSerializer.tryImport(file).thenAccept(area -> {
                if (area == null) {
                    plugin.getMessages().sendMessage(sender, "import.failed", filePlaceholder, serializerPlaceholder);
                    return;
                }

                plugin.getMessages().sendMessage(sender, "import.success", filePlaceholder, serializerPlaceholder);

                plugin.getContainerRegistry().getDefaultContainer().saveTemplate(TextUtils.removeFileExtension(fileName), area).thenRun(() -> {
                    plugin.getMessages().sendMessage(sender, "import.save-success", filePlaceholder, serializerPlaceholder);
                });
            });
        }

    }


}
