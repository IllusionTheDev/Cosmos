package me.illusion.cosmos.utilities.menu.registry.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import me.illusion.cosmos.utilities.menu.base.BaseMenu;

@Getter
public class HiddenMenuData {

    private final BaseMenu menu;
    private final Map<String, Object> meta = new HashMap<>();

    private final List<Runnable> displayTasks = new ArrayList<>();

    public HiddenMenuData(BaseMenu menu) {
        this.menu = menu;
    }

    public void addDisplayTask(Runnable task) {
        displayTasks.add(task);
    }

    public void runDisplayTasks() {
        displayTasks.forEach(Runnable::run);
    }

    public <T> T getMeta(String key, Class<T> type) {
        Object object = meta.get(key);

        if (object == null) {
            return null;
        }

        return type.cast(object);
    }

    public void addMeta(String key, Object value) {
        meta.put(key, value);
    }

}
