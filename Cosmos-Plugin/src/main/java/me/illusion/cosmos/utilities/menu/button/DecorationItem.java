package me.illusion.cosmos.utilities.menu.button;

import java.util.Collection;
import java.util.function.Consumer;
import me.illusion.cosmos.utilities.text.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class DecorationItem extends Button {

    public DecorationItem(ItemStack displayItem,
        Consumer<InventoryClickEvent> clickTask,
        Consumer<InventoryClickEvent> rightClickTask,
        Consumer<InventoryClickEvent> leftClickTask,
        Collection<Placeholder<Player>> placeholders) {
        super(displayItem, clickTask, rightClickTask, leftClickTask, placeholders);
    }

    public DecorationItem(ItemStack displayItem) {
        super(displayItem);
    }

    public DecorationItem(ItemStack displayItem,
        Consumer<InventoryClickEvent> clickTask) {
        super(displayItem, clickTask);
    }

    @Override
    public void handle(InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
