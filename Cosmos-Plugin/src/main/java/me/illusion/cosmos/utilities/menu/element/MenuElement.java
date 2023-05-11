package me.illusion.cosmos.utilities.menu.element;

import java.util.Collection;
import java.util.Collections;
import me.illusion.cosmos.utilities.text.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface MenuElement {

    ItemStack getDisplayItem();

    void handle(InventoryClickEvent event);

    MenuElement copy();

    default Collection<Placeholder<Player>> getItemPlaceholders() {
        return Collections.emptyList();
    }

    default MenuElement setItemPlaceholders(Collection<Placeholder<Player>> placeholders) {
        return this;
    }

}
