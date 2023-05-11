package me.illusion.cosmos.utilities.menu.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.illusion.cosmos.utilities.concurrency.MainThreadExecutor;
import me.illusion.cosmos.utilities.item.ItemUtil;
import me.illusion.cosmos.utilities.menu.element.MenuElement;
import me.illusion.cosmos.utilities.menu.element.Renderable;
import me.illusion.cosmos.utilities.text.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class BaseMenu implements InventoryHolder {

    private final UUID destinationPlayerId;
    private final List<Renderable> renderables = new ArrayList<>();
    private final Map<Integer, MenuElement> elements = new HashMap<>();
    private final List<Placeholder<Player>> titlePlaceholders = new ArrayList<>();
    private final String title;
    private final Inventory inventory;
    private final List<Runnable> closeTasks = new ArrayList<>();
    private boolean allowRemoveItems = false;


    public BaseMenu(UUID playerId, String title, int rows) {
        this.title = title;
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
        this.destinationPlayerId = playerId;
    }


    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        if (!Bukkit.isPrimaryThread()) {
            MainThreadExecutor.INSTANCE.execute(this::open);
            return;
        }

        Player player = getPlayer();

        if (player == null) {
            return;
        }
        forceUpdate();
        player.openInventory(inventory);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(destinationPlayerId);
    }

    public void addRenderable(Renderable... renderable) {
        renderables.addAll(List.of(renderable));
    }

    public void forceUpdate() {
        Player player = getPlayer();

        if (player == null) {
            return;
        }

        for (Renderable renderable : renderables) {
            if (renderable.isActive()) {
                renderable.forceUpdate();
            }
        }

        for (Map.Entry<Integer, MenuElement> entry : elements.entrySet()) {
            int slot = entry.getKey();
            MenuElement element = entry.getValue();

            ItemStack item = element.getDisplayItem().clone();

            ItemUtil.replacePlaceholder(item, player, element.getItemPlaceholders());
            inventory.setItem(slot, item);
        }
    }

    public void setElement(int slot, MenuElement element) {
        elements.put(slot, element);
    }

    public void onClose(Runnable task) {
        closeTasks.add(task);
    }

    public boolean isAllowRemoveItems() {
        return allowRemoveItems;
    }

    public void setAllowRemoveItems(boolean allowRemoveItems) {
        this.allowRemoveItems = allowRemoveItems;
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot < 0 || slot >= inventory.getSize()) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }

        MenuElement element = elements.get(slot);

        if (element == null) {
            return;
        }

        element.handle(event);

        if (!allowRemoveItems) {
            event.setCancelled(true);
        }
    }

    public void handleClose() {
        for (Runnable task : closeTasks) {
            task.run();
        }
    }
}
