package me.illusion.cosmos.utilities.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import me.illusion.cosmos.utilities.text.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public final class ItemUtil {

    private ItemUtil() {

    }

    public static void give(Player player, ItemStack item) {
        PlayerInventory inv = player.getInventory();

        for (Map.Entry<Integer, ItemStack> entry : inv.addItem(item).entrySet()) {
            ItemStack copy = entry.getValue().clone();
            item.setAmount(entry.getKey());
            player.getWorld().dropItemNaturally(player.getLocation(), copy);
        }
    }

    public static void setModelData(ItemStack item, int modelData) {
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(modelData);
        item.setItemMeta(meta);
    }

    public static <T> void replacePlaceholder(ItemStack item, T replacementObject,
        Collection<Placeholder<T>> placeholderCollection) {
        if (item == null || item.getItemMeta() == null) {
            return;
        }

        if (placeholderCollection == null || placeholderCollection.isEmpty()) {
            return;
        }

        Placeholder<T>[] placeholders = new Placeholder[placeholderCollection.size()];

        int index = 0;
        for (Placeholder<T> placeholder : placeholderCollection) {
            if (placeholder == null) {
                continue;
            }

            placeholders[index++] = placeholder;
        }

        // shrink array to fit
        if (index != placeholders.length) {
            Placeholder<T>[] newPlaceholders = new Placeholder[index];
            System.arraycopy(placeholders, 0, newPlaceholders, 0, index);
            placeholders = newPlaceholders;
        }

        replacePlaceholder(item, replacementObject, placeholders);
    }

    @SafeVarargs
    public static synchronized <T> void replacePlaceholder(ItemStack item, T replacementObject,
        Placeholder<T>... placeholder) {
        if (item == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return;
        }

        if (meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();

            for (Placeholder<T> placeholder1 : placeholder) {
                displayName = placeholder1.replace(displayName, replacementObject);
            }

            meta.setDisplayName(displayName);
        }

        if (meta.hasLore()) {
            List<String> lore = meta.getLore();

            for (int index = 0; index < lore.size(); index++) {
                String line = lore.get(index);

                for (Placeholder<T> placeholder1 : placeholder) {
                    line = placeholder1.replace(line, replacementObject);
                }

                lore.set(index, line);
            }

            meta.setLore(lore);
        }

        if (meta instanceof SkullMeta skullMeta) {

            boolean replaceProfile = true;
            if (skullMeta.hasOwner()) {
                String owner = skullMeta.getOwner();

                for (Placeholder<T> placeholder1 : placeholder) {
                    String oldOwner = owner;
                    owner = placeholder1.replace(owner, replacementObject);

                    if (owner != null && !owner.equals(oldOwner)) {
                        skullMeta.setOwner(owner);
                        replaceProfile = false;
                    }
                }

            }

            if (replaceProfile) {
                try {

                    Field profileField = skullMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    GameProfile profile = (GameProfile) profileField.get(skullMeta);
                    profileField.setAccessible(false);

                    if (profile != null) {
                        PropertyMap propertyMap = profile.getProperties();
                        Collection<Property> properties = new HashSet<>(
                            propertyMap.get("textures")
                        );

                        propertyMap.removeAll("properties");

                        for (Property property : properties) {
                            String value = property.getValue();
                            String signature = property.getSignature();

                            for (Placeholder<T> placeholder1 : placeholder) {
                                value = placeholder1.replace(value, replacementObject);
                                signature = placeholder1.replace(signature, replacementObject);
                            }

                            propertyMap.put("textures", new Property("textures", value, null));
                            break;
                        }

                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }


        }
        item.setItemMeta(meta);
    }

}
