package me.illusion.cosmos.command;

import java.io.File;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.utilities.command.command.impl.AdvancedCommand;
import me.illusion.cosmos.utilities.command.command.impl.ExecutionContext;
import me.illusion.cosmos.utilities.text.Placeholder;
import me.illusion.cosmos.utilities.text.TextUtils;
import org.bukkit.command.CommandSender;

public class CosmosImportCommand extends AdvancedCommand {

    private final CosmosPlugin plugin;

    public CosmosImportCommand(CosmosPlugin plugin) {
        super("cosmos import <serializer> <filename>");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, ExecutionContext context) {
        String serializer = context.getParameter("serializer");
        String fileName = context.getParameter("filename");

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
