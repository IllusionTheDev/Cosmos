package me.illusion.cosmos.utilities.commandv2.language.type;

public interface ParameterType<Type> {

    boolean isType(String input);

    Type parse(String input);

    Type getDefaultValue();

}
