package me.illusion.cosmos.utilities.commandv2.language.type.impl.smart;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.illusion.cosmos.utilities.commandv2.language.type.impl.StringParameterType;

public class ConditionParameterType extends StringParameterType {

    private static final Pattern CONDITION_PATTERN = Pattern.compile("(.+)([=<>]+)(.+)");

    private static final Map<Character, BiPredicate<String, String>> OPERATORS = Map.of(
        '=', String::equalsIgnoreCase,
        '>', (a, b) -> a.compareTo(b) > 0,
        '<', (a, b) -> a.compareTo(b) < 0
    );

    @Override
    public boolean isType(String input) {
        return CONDITION_PATTERN.matcher(input).matches();
    }

    @Override
    public String parse(String input) {
        Matcher matcher = CONDITION_PATTERN.matcher(input);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid condition: " + input);
        }

        String left = matcher.group(1);
        char operator = matcher.group(2).charAt(0);
        String right = matcher.group(3);

        if (left.equalsIgnoreCase(right) && operator == '=' && matchesVariableFormat(left)) {
            // If both sides are the same, and the operator is an equals
            // Which means we're in a case where we're doing something like if {var} = {var}
            // Which is usually used to check if a variable is set
            System.err.println("[SMART] Checkstyle warning: " + input
                + " is a redundant condition, it is recommended to use a \"setdefault <variable> to <value>\" statement instead.");
        }

        return input;
    }

    private boolean matchesVariableFormat(String input) {
        int start = input.indexOf('{');
        int end = input.indexOf('}');

        if (start == -1 || end == -1) {
            return false;
        }

        return start < end;
    }
}
