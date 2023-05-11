package me.illusion.cosmos.utilities.menu.selection;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import me.illusion.cosmos.utilities.menu.mask.PatternMask;

@Builder
public class PatternMaskSelection implements Selection {

    private final PatternMask pattern;
    private final String mask;

    public PatternMaskSelection(PatternMask mask, String selection) {
        this.pattern = mask;
        this.mask = selection;
    }

    public static PatternMaskSelection of(PatternMask mask, String selection) {
        return new PatternMaskSelection(mask, selection);
    }

    @Override
    public List<Integer> getSlots() {
        List<Integer> slots = new ArrayList<>();

        for (int row = 0; row < pattern.getPattern().size(); row++) {
            String line = pattern.getPattern().get(row);

            if (line.length() == 9) {
                char character = mask.charAt(0);

                for (int index = 0; index < line.length(); index++) {
                    if (line.charAt(index) == character) {
                        slots.add(index + (row * 9));
                    }
                }
            } else {
                String[] split = line.split(" ");

                for (int index = 0; index < Math.min(split.length, 9); index++) {
                    if (split[index].equals(mask)) {
                        slots.add(index + (row * 9));
                    }
                }
            }
        }

        return slots;
    }
}
