package me.illusion.cosmos.utilities.storage;

import java.io.File;
import java.util.Collection;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import me.illusion.cosmos.utilities.text.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MessagesFile extends YMLBase {

    private final Pattern pattern;
    @Getter
    private final String prefix, arrow;

    private String msg;

    public MessagesFile(JavaPlugin plugin) {
        super(plugin, new File(plugin.getDataFolder(), "messages.yml"), true);
        pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        prefix = getConfiguration().getString("messages.prefix");
        arrow = getConfiguration().getString("messages.arrow");

        writeUnsetValues();

    }

    public String setColor(String message) {
        String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        int minorVer = Integer.parseInt(split[1]);

        if (minorVer >= 16) {
            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                String color = message.substring(matcher.start(), matcher.end());
                message = message.replace(color, net.md_5.bungee.api.ChatColor.of(color) + "");
                matcher = pattern.matcher(message);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private String setMessage(String name) {
        return setMessage(name, (s) -> s);
    }

    private String setMessage(String name, Function<String, String> action) {
        if (!getConfiguration().contains("messages." + name)) {
            return "";
        }
        msg = getMessage(name).replace("%prefix%", prefix);

        msg = action.apply(msg);
        return setColor(msg);
    }

    public void sendStringMessage(CommandSender player, String msg) {
        player.sendMessage(setColor(msg));
    }

    public void sendMessage(CommandSender sender, String name) {
        sendMessage(sender, name, (s) -> s);
    }


    @SafeVarargs
    public final <T extends CommandSender> void sendMessage(T sender, String name, Placeholder<T>... placeholders) {
        sendMessage(sender, name, (text) -> {
            for (Placeholder<T> placeholder : placeholders) {
                text = placeholder.replace(text, sender);
            }

            return text;
        });
    }

    public final <T extends CommandSender> void sendMessage(T sender, String name, Collection<Placeholder<T>> placeholders) {
        sendMessage(sender, name, (text) -> {
            for (Placeholder<T> placeholder : placeholders) {
                text = placeholder.replace(text, sender);
            }

            return text;
        });
    }


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

    public String getMessage(String name) {
        return getConfiguration().getString("messages." + name);
    }

    public void reload() {
        super.reload();
    }


}

