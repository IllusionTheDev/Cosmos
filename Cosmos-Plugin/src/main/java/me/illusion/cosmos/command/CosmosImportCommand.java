package me.illusion.cosmos.command;

import java.io.File;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.serialization.CosmosSerializer;
import me.illusion.cosmos.utilities.command.command.impl.AdvancedCommand;
import me.illusion.cosmos.utilities.command.command.impl.ExecutionContext;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import me.illusion.cosmos.utilities.text.Placeholder;
import me.illusion.cosmos.utilities.text.TextUtils;
import org.bukkit.command.CommandSender;

public class CosmosImportCommand extends AdvancedCommand {

    private final CosmosPlugin plugin;
    private final MessagesFile messages;

    public CosmosImportCommand(CosmosPlugin plugin) {
        super("cosmos import <serializer> <filename>");

        this.plugin = plugin;
        this.messages = plugin.getMessages();

        addInputValidation("serializer", sender -> messages.sendMessage(sender, "import.invalid-serializer-arg"));
        addInputValidation("filename", sender -> messages.sendMessage(sender, "import.invalid-file-arg"));
    }

    @Override
    public void execute(CommandSender sender, ExecutionContext context) {
        String serializer = context.getParameter("serializer");
        String fileName = context.getParameter("filename");

        CosmosSerializer cosmosSerializer = plugin.getSerializerRegistry().get(serializer);

        if (cosmosSerializer == null) {
            messages.sendMessage(sender, "import.invalid-serializer");
            return;
        }

        File file = new File(plugin.getDataFolder() + File.separator + "import", fileName);

        Placeholder<CommandSender> filePlaceholder = new Placeholder<>("%file%", fileName);
        Placeholder<CommandSender> serializerPlaceholder = new Placeholder<>("%serializer%", serializer);

        if (!file.exists()) {
            messages.sendMessage(sender, "import.invalid-file", filePlaceholder);
            return;
        }

        cosmosSerializer.tryImport(file).thenAccept(area -> {
            if (area == null) {
                messages.sendMessage(sender, "import.failed", filePlaceholder, serializerPlaceholder);
                return;
            }

            messages.sendMessage(sender, "import.success", filePlaceholder, serializerPlaceholder);

            plugin.getContainerRegistry().getDefaultContainer().saveTemplate(TextUtils.removeFileExtension(fileName), area).thenRun(() -> {
                messages.sendMessage(sender, "import.save-success", filePlaceholder, serializerPlaceholder);
            });
        });
    }

}
