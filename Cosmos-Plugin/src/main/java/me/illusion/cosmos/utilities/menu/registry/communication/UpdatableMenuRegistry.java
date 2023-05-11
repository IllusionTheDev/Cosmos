package me.illusion.cosmos.utilities.menu.registry.communication;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.entity.Player;

public class UpdatableMenuRegistry {

    private final Set<UpdatableMenu> menus = Sets.newConcurrentHashSet();

    public void refreshIf(String identifier, Predicate<Player> viewerPredicate) {
        refreshIf(List.of(identifier), viewerPredicate);
    }

    public void refreshIf(Collection<? extends CharSequence> identifiers, Predicate<Player> viewerPredicate) {
        for (UpdatableMenu menu : menus) {
            if (menu.getIdentifier() == null) {
                continue;
            }
            if (!identifiers.contains(menu.getIdentifier())) {
                continue;
            }
            if (!viewerPredicate.test(menu.getViewer())) {
                continue;
            }

            menu.refresh();
        }
    }

    public void closeIf(String identifier, Predicate<Player> viewerPredicate) {
        closeIf(List.of(identifier), viewerPredicate);
    }

    public void closeIf(Collection<? extends CharSequence> identifiers, Predicate<Player> viewerPredicate) {
        for (UpdatableMenu menu : menus) {
            if (menu.getIdentifier() == null) {
                continue;
            }
            if (!identifiers.contains(menu.getIdentifier())) {
                continue;
            }
            if (!viewerPredicate.test(menu.getViewer())) {
                continue;
            }

            menu.close();
        }
    }


    public void refreshIf(String identifier, Predicate<UpdatableMenu> menuPredicate, Predicate<Player> viewerPredicate) {
        refreshIf(List.of(identifier), menuPredicate, viewerPredicate);
    }

    public void refreshIf(Collection<? extends CharSequence> identifiers, Predicate<UpdatableMenu> menuPredicate, Predicate<Player> viewerPredicate) {
        for (UpdatableMenu menu : menus) {
            if (menu.getIdentifier() == null) {
                continue;
            }
            if (!identifiers.contains(menu.getIdentifier())) {
                continue;
            }
            if (!menuPredicate.test(menu)) {
                continue;
            }
            if (!viewerPredicate.test(menu.getViewer())) {
                continue;
            }

            menu.refresh();
        }
    }

    public void closeIf(String identifier, Predicate<UpdatableMenu> menuPredicate, Predicate<Player> viewerPredicate) {
        closeIf(List.of(identifier), menuPredicate, viewerPredicate);
    }

    public void closeIf(Collection<? extends CharSequence> identifiers, Predicate<UpdatableMenu> menuPredicate, Predicate<Player> viewerPredicate) {
        for (UpdatableMenu menu : menus) {
            if (menu.getIdentifier() == null) {
                continue;
            }
            if (!identifiers.contains(menu.getIdentifier())) {
                continue;
            }
            if (!menuPredicate.test(menu)) {
                continue;
            }
            if (!viewerPredicate.test(menu.getViewer())) {
                continue;
            }

            menu.close();
        }
    }

    public void register(UpdatableMenu menu) {
        menus.add(menu);
        menu.getMenu().onClose(() -> unregister(menu));
    }

    public void unregister(UpdatableMenu menu) {
        menus.remove(menu);
    }

}
