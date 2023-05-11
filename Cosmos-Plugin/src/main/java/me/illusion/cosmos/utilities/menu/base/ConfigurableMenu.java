package me.illusion.cosmos.utilities.menu.base;

import java.util.List;
import java.util.UUID;
import me.illusion.cosmos.utilities.menu.configuration.ConfigurationApplicator;
import me.illusion.cosmos.utilities.menu.element.MenuElement;
import me.illusion.cosmos.utilities.menu.mask.PatternMask;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class ConfigurableMenu extends BaseMenu {

    private final ConfigurationApplicator applicator;


    public ConfigurableMenu(UUID playerId, String title, int rows, ConfigurationApplicator applicator) {
        super(playerId, title, rows);
        this.applicator = applicator;
    }

    public ConfigurationApplicator getApplicator() {
        return applicator;
    }

    public ItemStack getItem(String key) {
        return getApplicator().getItem(key);
    }

    public MenuElement getDecorationItem(String key) {
        return getApplicator().getDecorationItem(key);
    }

    public PatternMask getMask() {
        return getApplicator().getMask();
    }

    public List<String> getDescription() {
        return getApplicator().getDescription();
    }

    public FileConfiguration getConfig() {
        return getApplicator().getConfig();
    }


}
