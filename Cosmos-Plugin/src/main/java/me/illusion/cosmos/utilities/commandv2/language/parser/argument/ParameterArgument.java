package me.illusion.cosmos.utilities.commandv2.language.parser.argument;

import java.util.List;
import me.illusion.cosmos.utilities.commandv2.language.parser.ArgumentType;
import me.illusion.cosmos.utilities.commandv2.language.type.ParameterType;

public class ParameterArgument<Type> extends Argument<Type> {

    private final ParameterType<?> type;

    public ParameterArgument(String name, ParameterType<?> type, boolean optional, Object value) {
        super(name, value instanceof List<?> ? ArgumentType.LIST : ArgumentType.PARAMETER, optional, value);
        this.type = type;
    }

    public ParameterType<?> getType() {
        return type;
    }

    @Override
    public ParameterArgument<Type> clone() {
        return new ParameterArgument<>(getName(), type, isOptional(), getValue());
    }
}
