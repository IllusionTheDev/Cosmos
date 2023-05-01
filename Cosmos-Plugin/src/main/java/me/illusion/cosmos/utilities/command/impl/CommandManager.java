package me.illusion.cosmos.utilities.command.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.illusion.cosmos.utilities.command.BaseCommand;
import me.illusion.cosmos.utilities.command.SimpleCommand;
import me.illusion.cosmos.utilities.command.comparison.ComparisonResult;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandManager {

    private static CommandMap commandMap;
    private static Constructor<PluginCommand> pluginCommandConstructor;

    static {
        try {
            Server server = Bukkit.getServer();
            Field commandMapField = server.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            commandMap = (CommandMap) commandMapField.get(server);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            pluginCommandConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private final Set<String> registeredBaseCommands = new HashSet<>();
    private final Map<String, SimpleCommand> commands = new HashMap<>();

    private final JavaPlugin plugin;
    private final MessagesFile messages;

    public CommandManager(JavaPlugin plugin, MessagesFile messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public void register(SimpleCommand command) {
        if (command.getIdentifier() == null) {
            System.err.println("Command " + command.getClass().getSimpleName() + " has no identifier!");
            return;
        }

        if (command.getIdentifier().startsWith("*")) {
            throw new IllegalArgumentException("Command identifier cannot start with *");
        }

        int wildcardCount = 0;

        for (char c : command.getIdentifier().toCharArray()) {
            if (c == '*') {
                wildcardCount++;
            }
        }

        for (String alias : command.getAliases()) {
            if (alias.startsWith("*")) {
                throw new IllegalArgumentException("Command alias cannot start with *");
            }

            int aliasWildcardCount = 0;

            for (char c : alias.toCharArray()) {
                if (c == '*') {
                    aliasWildcardCount++;
                }
            }

            if (aliasWildcardCount != wildcardCount) {
                throw new IllegalArgumentException(
                    "Command alias " + alias + " does not have the same amount of wildcards as the command identifier " + command.getIdentifier());
            }
        }

        registerCommand(command.getIdentifier(), command);

        for (String alias : command.getAliases()) {
            registerCommand(alias, command);
        }
    }

    private void registerCommand(String identifier, SimpleCommand command) {
        commands.put(identifier, command);

        String base = getBaseCommand(identifier);

        if (!registeredBaseCommands.contains(base)) {
            try {
                PluginCommand pluginCommand = pluginCommandConstructor.newInstance(base, plugin);

                BaseCommand baseCommand = new BaseCommand(messages, this);

                pluginCommand.setExecutor(baseCommand);
                pluginCommand.setTabCompleter(baseCommand);

                commandMap.register(base, pluginCommand);

            } catch (InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                e.printStackTrace();
            }

            System.out.println(base + " was registered as a command");
            registeredBaseCommands.add(base);
        }
    }

    public SimpleCommand get(String identifier) {
        String[] split = identifier.split("\\.");
        String base = split[0];

        if (split.length == 1) {
            return commands.get(base);
        }

        for (String key : commands.keySet()) {
            if (key.startsWith(base + ".")) {
                String[] splitKey = key.split("\\.");
                String[] splitIdentifier = identifier.split("\\.");

                boolean matches = true;

                for (int i = 0; i < splitKey.length; i++) {
                    if (splitKey[i].equals("*")) {
                        continue;
                    }

                    if (splitKey[i].equals(splitIdentifier[i])) {
                        continue;
                    }

                    matches = false;
                    break;
                }

                if (matches) {
                    return commands.get(key);
                }
            }
        }

        return null;
    }

    public List<String> tabComplete(String identifier) {
        ComparisonResult result = new ComparisonResult(commands);
        return result.tabComplete(identifier);
    }

    public SimpleCommand get(String name, String... args) {
        String identifier = String.join(".", name, String.join(".", args));

        return get(identifier);
    }

    private String getBaseCommand(String identifier) {
        int index = identifier.indexOf(".");
        return index == -1 ? identifier : identifier.substring(0, index);
    }

}
