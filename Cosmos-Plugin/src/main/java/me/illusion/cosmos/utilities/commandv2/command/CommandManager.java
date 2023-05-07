package me.illusion.cosmos.utilities.commandv2.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.illusion.cosmos.utilities.command.SimpleCommand;
import me.illusion.cosmos.utilities.commandv2.command.compat.SimpleCommandWrapper;
import me.illusion.cosmos.utilities.commandv2.language.AbstractObjectiveModel;
import me.illusion.cosmos.utilities.commandv2.language.CompiledObjective;
import me.illusion.cosmos.utilities.geometry.Pair;
import me.illusion.cosmos.utilities.storage.MessagesFile;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandManager {

    private final List<AbstractObjectiveModel<?>> commands = new ArrayList<>();
    private final Map<String, BukkitBaseCommand> bukkitCommands = new HashMap<>();

    private final MessagesFile messages;
    private final JavaPlugin plugin;

    public CommandManager(JavaPlugin plugin, MessagesFile messages) {
        this.messages = messages;
        this.plugin = plugin;
    }

    public void registerCommand(AbstractObjectiveModel<?> model) {
        commands.add(model);
        attemptCreateCommand(model);
    }

    private void attemptCreateCommand(AbstractObjectiveModel<?> model) {
        String firstWord = model.getSyntax().split(" ")[0];

        if (bukkitCommands.containsKey(firstWord)) {
            return;
        }

        BukkitBaseCommand command = new BukkitBaseCommand(this);
        bukkitCommands.put(firstWord, command);

        CommandUtilities.registerCommand(firstWord, plugin, command);
    }


    public void registerCommand(SimpleCommand legacy) {
        registerCommand(new SimpleCommandWrapper(legacy));
    }

    public List<String> tabComplete(String line) {
        System.out.println("tabComplete: \"" + line + "\"");

        // remove double spaces and all
        line = line.replaceAll(" +", " ");

        List<String> completions = new ArrayList<>();

        for (AbstractObjectiveModel<?> command : commands) {
            completions.addAll(command.getSuggestions(line));
        }

        return completions;
    }

    public Pair<AbstractObjectiveModel<?>, CompiledObjective> parse(String line) {
        for (AbstractObjectiveModel<?> command : commands) {
            CompiledObjective compiled = command.parse(line);

            if (compiled == null) {
                continue;
            }

            return new Pair<>(command, compiled);
        }

        return null;
    }

    public MessagesFile getMessages() {
        return messages;
    }
}
