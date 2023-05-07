package me.illusion.cosmos.utilities.commandv2.language.type.impl;

import me.illusion.cosmos.utilities.commandv2.language.type.ParameterType;


public class StringParameterType implements ParameterType<String> {

    @Override
    public boolean isType(String input) {
        return true;
    }

    @Override
    public String parse(String input) {
        return input;
    }

    @Override
    public String getDefaultValue() {
        return "";
    }
}
