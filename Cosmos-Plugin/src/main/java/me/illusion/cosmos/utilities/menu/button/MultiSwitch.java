package me.illusion.cosmos.utilities.menu.button;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import me.illusion.cosmos.utilities.menu.SwitchPredicate;
import me.illusion.cosmos.utilities.menu.element.MenuElement;
import me.illusion.cosmos.utilities.text.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

public class MultiSwitch<T> implements MenuElement {

    private final List<T> choices;
    private final List<Button> buttons;
    private SwitchPredicate switchIf;

    private int index = 0;

    private Consumer<T> choiceUpdateTask;

    public MultiSwitch(Button defaultButton, List<T> choices) {
        this.choices = choices;

        buttons = new ArrayList<>();

        for (int index = 0; index < choices.size(); index++) {
            buttons.add(defaultButton);
        }
    }

    public MultiSwitch(Button defaultButton, T... choices) {
        this.choices = List.of(choices);

        buttons = new ArrayList<>();

        for (int index = 0; index < choices.length; index++) {
            buttons.add(defaultButton);
        }
    }

    public MultiSwitch(List<Button> buttons, List<T> choices) {
        this.choices = choices;
        this.buttons = buttons;

        if (buttons.size() != choices.size()) {
            throw new IllegalArgumentException("The amount of buttons must be equal to the amount of choices!");
        }
    }

    public MultiSwitch(Map<Button, T> choices) {
        this.choices = new ArrayList<>();
        this.buttons = new ArrayList<>();

        for (Map.Entry<Button, T> entry : choices.entrySet()) {
            buttons.add(entry.getKey());
            this.choices.add(entry.getValue());
        }

        if (buttons.size() != choices.size()) {
            throw new IllegalArgumentException("The amount of buttons must be equal to the amount of choices!");
        }
    }

    public MultiSwitch(List<Button> buttons, T... choices) {
        this.choices = List.of(choices);
        this.buttons = buttons;

        if (buttons.size() != choices.length) {
            throw new IllegalArgumentException("The amount of buttons must be equal to the amount of choices!");
        }
    }


    public void cycle() {
        index = (index + 1) % choices.size();
        System.out.println("Cycling to " + index);
        runUpdate();
    }

    public void cycleBack() {
        index = (index - 1) % choices.size();

        if (index < 0) {
            index = choices.size() - 1;
        }

        System.out.println("Cycling back to " + index);
        runUpdate();
    }

    public void reset() {
        index = 0;
        runUpdate();
    }

    public void setChoice(T choice) {
        index = choices.indexOf(choice);
        runUpdate();
    }

    public void setChoice(int index) {
        this.index = index;
        runUpdate();
    }

    public T getSelectedChoice() {
        return choices.get(index);
    }

    public void switchIf(SwitchPredicate predicate) {
        this.switchIf = predicate;
    }

    public void addChoice(T choice) {
        choices.add(choice);

        if (buttons.isEmpty()) {
            throw new IllegalStateException("No default button was provided!");
        }

        buttons.add(buttons.get(0));
    }

    public void addChoice(T choice, Button button) {
        choices.add(choice);
        buttons.add(button);
    }

    public void removeChoice(T choice) {
        if (index == choices.size() - 1) {
            index--;
            runUpdate();
        }

        buttons.remove(index + 1);
        choices.remove(choice);
    }

    public void onChoiceUpdate(Consumer<T> task) {
        this.choiceUpdateTask = task;
    }

    private void runUpdate() {
        if (choiceUpdateTask != null) {
            choiceUpdateTask.accept(getSelectedChoice());
        }
    }


    @Override
    public ItemStack getDisplayItem() {
        return buttons.get(index).getDisplayItem();
    }

    @Override
    public Collection<Placeholder<Player>> getItemPlaceholders() {
        return buttons.get(index).getItemPlaceholders();
    }

    @Override
    public MenuElement setItemPlaceholders(Collection<Placeholder<Player>> placeholders) {
        for (Button button : buttons) {
            button.setItemPlaceholders(placeholders);
        }

        return this;
    }

    @Override
    public void handle(InventoryClickEvent event) {
        boolean shouldSwitch = true;
        if (switchIf != null) {
            shouldSwitch = switchIf.switchIf();
        }

        buttons.get(index).handle(event);

        if (shouldSwitch) {
            if (!event.isCancelled()) {
                if (event.isLeftClick()) {
                    cycle();
                } else if (event.isRightClick()) {
                    cycleBack();
                }
            }
        }
    }

    @Override
    public MenuElement copy() {
        MultiSwitch<T> tMultiSwitch = new MultiSwitch<>(buttons, choices);
        tMultiSwitch.choiceUpdateTask = choiceUpdateTask;

        return tMultiSwitch;
    }
}
