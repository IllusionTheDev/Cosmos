package me.illusion.cosmos.utilities.menu.mask;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import me.illusion.cosmos.utilities.menu.selection.MultiSelection;
import me.illusion.cosmos.utilities.menu.selection.PatternMaskSelection;
import me.illusion.cosmos.utilities.menu.selection.Selection;

@Getter
public class PatternMask {

    private final List<String> pattern;

    public PatternMask(List<String> pattern) {
        this.pattern = pattern;
    }

    public PatternMask(String... pattern) {
        this.pattern = List.of(pattern);
    }

    public static PatternMask of(List<String> pattern) {
        return new PatternMask(pattern);
    }

    public static PatternMask of(String... pattern) {
        return new PatternMask(pattern);
    }

    private boolean contains(char character) {
        for (String line : pattern) {
            if (line.contains(String.valueOf(character))) {
                return true;
            }
        }

        return false;
    }

    public PatternMaskSelection selection(char character) {
        return new PatternMaskSelection(this, String.valueOf(character));
    }

    public PatternMaskSelection selection(String character) {
        return new PatternMaskSelection(this, character);
    }

    public MultiSelection multiSelection(char... characters) {
        return multiSelection(new String(characters));
    }

    public MultiSelection multiSelection(String characters) {
        List<Selection> selections = new ArrayList<>();

        for (char character : characters.toCharArray()) {
            if (!contains(character)) {
                throw new IllegalArgumentException("The mask does not contain the character '" + character + "'");
            }

            selections.add(selection(character));
        }

        return new MultiSelection(selections);
    }

}

/*
  - 'e . . . . . . . e'
  - 'p . . . . . . . n'
  - 'p . . . . . . . n'
  - 'e . . . . . . . e'
  - 'e e e e e e e e e'
 */