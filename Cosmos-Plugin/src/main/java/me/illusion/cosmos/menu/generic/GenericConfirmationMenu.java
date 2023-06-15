package me.illusion.cosmos.menu.generic;

import java.util.Collection;
import java.util.UUID;
import me.illusion.cosmos.CosmosPlugin;
import me.illusion.cosmos.utilities.menu.base.BaseMenu;
import me.illusion.cosmos.utilities.menu.base.ConfigurableMenu;
import me.illusion.cosmos.utilities.menu.configuration.ConfigurationApplicator;
import me.illusion.cosmos.utilities.menu.layer.BaseLayer;
import me.illusion.cosmos.utilities.menu.registry.communication.UpdatableMenu;
import me.illusion.cosmos.utilities.text.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GenericConfirmationMenu implements UpdatableMenu {

    private final CosmosPlugin plugin;

    private final String menuName;
    private final UUID viewerId;

    private Runnable confirmTask;
    private Runnable denyTask;

    private BaseMenu menu;
    private BaseLayer baseLayer;

    public GenericConfirmationMenu(CosmosPlugin plugin, String menuName, Player player) {
        this.plugin = plugin;
        this.menuName = menuName;
        this.viewerId = player.getUniqueId();

        setup();

        plugin.getMenuRegistry().getUpdatableMenuRegistry().register(this);
    }

    private void setup() {
        Player viewer = getViewer();

        ConfigurableMenu baseMenu = (ConfigurableMenu) plugin.getMenuRegistry().create(menuName, viewer);
        this.menu = baseMenu;

        ConfigurationApplicator applicator = baseMenu.getApplicator();
        baseLayer = new BaseLayer(baseMenu);

        applicator.registerButton(baseLayer, "confirm", () -> {
            runTask(confirmTask);
        });

        applicator.registerButton(baseLayer, "deny", () -> {
            runTask(denyTask);
        });

        baseMenu.addRenderable(baseLayer);
    }

    // --- Runnable stuff ---

    private Runnable associateTask(Runnable old, Runnable newTask) {
        return old == null ? newTask : () -> {
            old.run();
            newTask.run();
        };
    }

    public void onConfirm(Runnable task) {
        this.confirmTask = associateTask(confirmTask, task);
    }

    public void onDeny(Runnable task) {
        this.denyTask = associateTask(denyTask, task);
    }

    public void setPlaceholders(Collection<Placeholder<Player>> placeholders) {
        baseLayer.setItemPlaceholders(placeholders);
        menu.forceUpdate();
    }

    private void runTask(Runnable task) {
        if (task != null) {
            task.run();
        }
    }

    // UpdatableMenu stuff
    @Override
    public Player getViewer() {
        return Bukkit.getPlayer(viewerId);
    }

    @Override
    public void refresh() {
        // this is generic, no need for a refresh
    }

    @Override
    public BaseMenu getMenu() {
        return menu;
    }

    @Override
    public String getIdentifier() {
        return menuName + "-confirmation";
    }
}
