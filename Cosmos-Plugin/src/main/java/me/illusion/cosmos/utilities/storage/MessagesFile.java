package me.illusion.cosmos.utilities.storage;

import java.io.File;
import java.util.Collection;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.Getter;
import me.illusion.cosmos.utilities.text.Placeholder;
import me.illusion.cosmos.utilities.text.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class is responsible for handling the loading and saving of the messages.yml file. It also provides a method for sending messages to players.
 */
public class MessagesFile extends YMLBase {

    private final Pattern pattern;
    @Getter
    private final String prefix, arrow;

    private String msg;

    /**
     * Creates a new MessagesFile instance.
     *
     * @param plugin The plugin instance
     */
    public MessagesFile(JavaPlugin plugin) {
        super(plugin, new File(plugin.getDataFolder(), "messages.yml"), true);
        pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        prefix = getConfiguration().getString("messages.prefix");
        arrow = getConfiguration().getString("messages.arrow");

        writeUnsetValues();

    }

    /**
     * Replaces all color codes in the specified message with their respective color.
     *
     * @param message The message
     * @return The message with color codes replaced
     */
    public String setColor(String message) {
        return TextUtils.color(message);
    }

    /**
     * Applies the specified function to the message with the specified name, and formats color codes.
     *
     * @param name   The name of the message
     * @param action The function to apply
     * @return The formatted message
     */
    private String setMessage(String name, Function<String, String> action) {
        if (!getConfiguration().contains("messages." + name)) {
            return "";
        }
        msg = getMessage(name).replace("%prefix%", prefix);

        msg = action.apply(msg);
        return setColor(msg);
    }

    /**
     * Sends the specified message to the specified player, with no placeholder manipulation.
     *
     * @param player The player
     * @param msg    The message
     */
    public void sendStringMessage(CommandSender player, String msg) {
        player.sendMessage(setColor(msg));
    }

    /**
     * Sends the specified message to the specified player, with placeholder manipulation.
     *
     * @param sender The player
     * @param name   The name of the message
     */
    public void sendMessage(CommandSender sender, String name) {
        sendMessage(sender, name, (s) -> s);
    }


    /**
     * Sends the specified message to the specified player, with placeholder manipulation.
     *
     * @param sender       The player
     * @param name         The name of the message
     * @param placeholders The placeholders to apply
     * @param <T>          The type of the player
     */
    @SafeVarargs
    public final <T extends CommandSender> void sendMessage(T sender, String name, Placeholder<T>... placeholders) {
        sendMessage(sender, name, (text) -> {
            for (Placeholder<T> placeholder : placeholders) {
                text = placeholder.replace(text, sender);
            }

            return text;
        });
    }

    /**
     * Sends the specified message to the specified player, with placeholder manipulation.
     *
     * @param sender       The player
     * @param name         The name of the message
     * @param placeholders The placeholders to apply
     * @param <T>          The type of the player
     */
    public final <T extends CommandSender> void sendMessage(T sender, String name, Collection<Placeholder<T>> placeholders) {
        sendMessage(sender, name, (text) -> {
            for (Placeholder<T> placeholder : placeholders) {
                text = placeholder.replace(text, sender);
            }

            return text;
        });
    }

    /**
     * Sends the specified message to the specified player, with placeholder manipulation.
     *
     * @param sender The player
     * @param name   The name of the message
     * @param action The function to apply
     */
    public void sendMessage(CommandSender sender, String name, Function<String, String> action) {
        if (getConfiguration().isList("messages." + name)) {
            for (String str : getConfiguration().getStringList("messages." + name)) {
                msg = str.replace("%prefix%", prefix);
                msg = action.apply(msg);
                sender.sendMessage(setColor(msg));
            }
            return;
        }

        msg = setMessage(name, action);
        sender.sendMessage(msg);
    }

    /**
     * Obtains a raw message from the messages.yml file.
     *
     * @param name The name of the message
     * @return The message
     */
    public String getMessage(String name) {
        return getConfiguration().getString("messages." + name);
    }

}

