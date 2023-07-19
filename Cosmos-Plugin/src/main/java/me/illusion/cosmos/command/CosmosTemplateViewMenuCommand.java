package me.illusion.cosmos.command;

import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.menu.TemplateViewMenu;
import me.illusion.cosmos.utilities.command.command.impl.AdvancedCommand;
import me.illusion.cosmos.utilities.command.command.impl.ExecutionContext;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmosTemplateViewMenuCommand extends AdvancedCommand {

    private final CosmosPlugin plugin;

    public CosmosTemplateViewMenuCommand(CosmosPlugin plugin) {
        super("cosmos templates view");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, ExecutionContext context) {
        if (!(sender instanceof Player player)) {
            return;
        }

        new TemplateViewMenu(plugin, player.getUniqueId()).open();
    }
}
