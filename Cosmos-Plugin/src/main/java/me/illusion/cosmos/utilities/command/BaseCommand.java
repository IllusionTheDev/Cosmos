package me.illusion.cosmos.utilities.command;

import java.util.Collections;
import java.util.List;
import me.illusion.cosmos.utilities.command.impl.CommandManager;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import me.illusion.cosmos.utilities.text.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class BaseCommand implements CommandExecutor, TabCompleter {

    private final MessagesFile messages;
    private final CommandManager commandManager;

    public BaseCommand(MessagesFile messages, CommandManager commandManager) {
        this.messages = messages;
        this.commandManager = commandManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String name, String[] args)
        throws IllegalArgumentException {
        if (args.length == 0) {
            return Collections.emptyList();
        }

        String identifier = String.join(".", name, String.join(".", args)).replace(" ", ".");
        return commandManager.tabComplete(identifier);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args) {
        for (int index = 0; index < args.length; index++) {
            args[index] = args[index].replace(".", "@DOT@");
        }

        String identifier = String.join(".", name, String.join(".", args));

        // remove trailing dots, this is not tab completion
        while (identifier.endsWith(".")) {
            identifier = identifier.substring(0, identifier.length() - 1);
        }

        SimpleCommand command = commandManager.get(identifier);

        if (command == null) {
            messages.sendMessage(sender, "command.invalid-args");
            return true;
        }

        String permission = command.getPermission();

        if (!command.canExecute(sender)) {
            messages.sendMessage(sender, "command.cannot-use",
                new Placeholder<>("permission", permission),
                new Placeholder<>("command", name));
            return true;
        }

        if (command.hasPermission() && !sender.hasPermission(command.getPermission())) {
            messages.sendMessage(sender, "command.no-permission",
                new Placeholder<>("permission", permission),
                new Placeholder<>("command", name));

            return true;
        }

        List<Integer> wildcards = command.getWildcards();

        String[] commandArgs = new String[wildcards.size()];

        for (int index = 0; index < wildcards.size(); index++) {
            int argsIndex = wildcards.get(index) - 1;

            String text;

            if (argsIndex < args.length) {
                text = args[argsIndex].replace("@DOT@", ".");
            } else {
                text = "";
            }

            commandArgs[index] = text;
        }

        command.execute(sender, commandArgs);
        return true;
    }


}