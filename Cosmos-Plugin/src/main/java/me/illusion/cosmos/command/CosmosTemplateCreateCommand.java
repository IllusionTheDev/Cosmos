package me.illusion.cosmos.command;

import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.database.CosmosDataContainer;
import me.illusion.cosmos.menu.TemplateCreationMenu;
import me.illusion.cosmos.menu.generic.GenericConfirmationMenu;
import me.illusion.cosmos.utilities.command.command.impl.AdvancedCommand;
import me.illusion.cosmos.utilities.command.command.impl.ExecutionContext;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import me.illusion.cosmos.utilities.text.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmosTemplateCreateCommand extends AdvancedCommand {
    private final CosmosPlugin plugin;
    private final MessagesFile messages;

    public CosmosTemplateCreateCommand(CosmosPlugin plugin) {
        super("cosmos templates create");

        this.plugin = plugin;
        this.messages = plugin.getMessages();
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

        new TemplateCreationMenu(plugin, player.getUniqueId());
    }
}
