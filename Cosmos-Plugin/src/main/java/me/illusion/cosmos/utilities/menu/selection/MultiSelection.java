package me.illusion.cosmos.utilities.menu.selection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MultiSelection implements Selection {

    private final List<Selection> selections;

    public MultiSelection(List<Selection> selections) {
        this.selections = selections;
    }

    public MultiSelection(Selection... selections) {
        this.selections = List.of(selections);
    }

    public static MultiSelection of(List<Selection> selections) {
        return new MultiSelection(selections);
    }

    public static MultiSelection of(Selection... selections) {
        return new MultiSelection(selections);
    }

    @Override
    public List<Integer> getSlots() {
        List<Integer> slots = new ArrayList<>();

        for (Selection selection : selections) {
            slots.addAll(selection.getSlots());
        }

        // Remove duplicates
        slots = new ArrayList<>(new HashSet<>(slots));

        return slots;
    }
}
