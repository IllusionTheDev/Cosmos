package me.illusion.cosmos.utilities.menu.registry.communication;

import me.illusion.cosmos.utilities.concurrency.MainThreadExecutor;
import me.illusion.cosmos.utilities.menu.base.BaseMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public interface UpdatableMenu {

    Player getViewer();

    void refresh();

    BaseMenu getMenu();

    default void close() {
        Player viewer = getViewer();

        if (viewer.getOpenInventory().getTopInventory().equals(getMenu().getInventory())) {
            if (Bukkit.isPrimaryThread()) {
                viewer.closeInventory();
            } else {
                MainThreadExecutor.INSTANCE.execute(viewer::closeInventory); // fuck you bukkit
            }
        }
    }

    String getIdentifier(); // return menu.getOriginalName() ?

    default void open() {
        getMenu().open();
    }
}
