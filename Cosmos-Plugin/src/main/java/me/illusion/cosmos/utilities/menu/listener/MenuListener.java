package me.illusion.cosmos.utilities.menu.listener;

import java.util.UUID;
import java.util.function.Consumer;
import me.illusion.cosmos.utilities.menu.base.BaseMenu;
import me.illusion.cosmos.utilities.menu.registry.MenuRegistry;
import me.illusion.cosmos.utilities.menu.registry.meta.HiddenMenuData;
import me.illusion.cosmos.utilities.menu.registry.meta.HiddenMenuTracker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public class MenuListener implements Listener {

    public static boolean REGISTERED = false;

    private final MenuRegistry registry;

    private MenuListener(MenuRegistry registry) {
        this.registry = registry;
    }

    public static void register(MenuRegistry registry) {
        if (REGISTERED) {
            return;
        }

        JavaPlugin plugin = registry.getPlugin();
        Bukkit.getPluginManager().registerEvents(new MenuListener(registry), plugin);
        REGISTERED = true;
    }

    @EventHandler
    private void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (!(holder instanceof BaseMenu baseMenu)) {
            return;
        }

        baseMenu.handleClick(event);
    }

    @EventHandler
    private void onClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (!(holder instanceof BaseMenu baseMenu)) {
            return;
        }

        baseMenu.handleClose();
    }

    @EventHandler
    private void onChat(AsyncPlayerChatEvent event) {
        /*component message = event.getMessage()

        if(!(message instanceof TextComponent text))
            return;

         */

        String content = event.getMessage();
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        HiddenMenuTracker tracker = registry.getHiddenMenuTracker();

        HiddenMenuData data = tracker.getHiddenMenu(uuid);

        if (data == null) {
            return;
        }

        tracker.removeHiddenMenu(uuid);

        if (content.equalsIgnoreCase("cancel")) {
            event.setMessage("");
            player.sendMessage("Cancelled");
            data.runDisplayTasks();
            return;
        }

        Consumer<String> task = data.getMeta("input-task", Consumer.class);

        if (task == null) {
            return;
        }

        task.accept(content);
        data.runDisplayTasks();

        event.setCancelled(true);
    }
}
