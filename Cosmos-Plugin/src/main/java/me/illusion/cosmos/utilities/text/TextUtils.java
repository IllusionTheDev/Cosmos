package me.illusion.cosmos.utilities.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

/**
 * Represents a utility class for text.
 */
public class TextUtils {

    private static final Pattern hexPattern = Pattern.compile("#([A-Fa-f0-9]){6}");
    private static final Pattern illegalCharactersPattern = Pattern.compile("[^A-Za-z0-9]+");

    /**
     * Colors a String, replacing all hex colors with their respective ChatColor.
     *
     * @param message The String to color
     * @return The colored String
     */
    public static String color(String message) {
        Matcher matcher = hexPattern.matcher(message);
        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, String.valueOf(ChatColor.of(color)));
            matcher = hexPattern.matcher(message);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Capitalizes a String. (e.g. "hello" -> "Hello")
     *
     * @param toCapitalize The String to capitalize
     * @return The capitalized String
     */
    public static String capitalize(String toCapitalize) {
        return toCapitalize.substring(0, 1).toUpperCase() + toCapitalize.substring(1);
    }

    /**
     * Returns whether a String contains any illegal characters, including white spaces.
     */
    public static boolean containsIllegalCharacters(String input) {
        return illegalCharactersPattern.matcher(input).find();
    }
}

