package me.illusion.cosmos.utilities.item;

import java.util.List;
import java.util.function.Consumer;
import org.bukkit.configuration.ConfigurationSection;

public class ItemConfigurationSerializer {

    public static void applySection(ItemBuilder builder, ConfigurationSection section) {
        applyIfPresent(section, "amount", Integer.class, builder::amount);
        applyIfPresent(section, "name", String.class, builder::name);
        applyIfPresent(section, "lore", List.class, builder::lore);
        applyIfPresent(section, "enchantments", ConfigurationSection.class, builder::enchants);
        applyIfPresent(section, "flags", List.class, builder::flags);
        applyIfPresent(section, "unbreakable", Boolean.class, builder::unbreakable);
        applyIfPresent(section, "skull-hash", String.class, builder::skullHash);
        applyIfPresent(section, "skull", String.class, builder::skull);
        applyIfPresent(section, "model-data", Integer.class, builder::modelData);
        applyIfPresent(section, "glow", Boolean.class, builder::glowing);
    }

    private static <T> void applyIfPresent(ConfigurationSection section, String key, Class<T> clazz, Consumer<T> consumer) {
        if (section.contains(key)) {
            T value = section.getObject(key, clazz);

            if (value == null) {
                return;
            }

            consumer.accept(value);
        }
    }

}
