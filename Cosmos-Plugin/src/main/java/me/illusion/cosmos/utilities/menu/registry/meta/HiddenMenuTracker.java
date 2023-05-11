package me.illusion.cosmos.utilities.menu.registry.meta;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import me.illusion.cosmos.utilities.menu.base.BaseMenu;
import org.bukkit.entity.Player;

public class HiddenMenuTracker {

    private final Map<UUID, HiddenMenuData> hiddenMenus = new ConcurrentHashMap<>();

    public void addHiddenMenu(UUID uuid, HiddenMenuData data) {
        hiddenMenus.put(uuid, data);
    }

    public HiddenMenuData getHiddenMenu(UUID uuid) {
        return hiddenMenus.get(uuid);
    }

    public void removeHiddenMenu(UUID uuid) {
        hiddenMenus.remove(uuid);
    }

    public boolean hasHiddenMenu(UUID uuid) {
        return hiddenMenus.containsKey(uuid);
    }

    public void clear() {
        hiddenMenus.clear();
    }

    public void holdForInput(BaseMenu menu, Consumer<String> action, boolean reopenMenu) {
        Player player = menu.getPlayer();

        HiddenMenuData data = new HiddenMenuData(menu);
        data.addMeta("input-task", action);

        if (reopenMenu) {
            data.addDisplayTask(menu::open);
        }

        addHiddenMenu(player.getUniqueId(), data);

        player.closeInventory();
    }

    public void holdForInput(BaseMenu menu, Consumer<String> action) {
        holdForInput(menu, action, true);
    }

}
