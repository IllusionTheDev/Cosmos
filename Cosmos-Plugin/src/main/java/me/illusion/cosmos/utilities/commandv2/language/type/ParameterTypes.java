package me.illusion.cosmos.utilities.commandv2.language.type;

import me.illusion.cosmos.utilities.commandv2.language.type.impl.IntegerParameterType;
import me.illusion.cosmos.utilities.commandv2.language.type.impl.NumericalParameterType;
import me.illusion.cosmos.utilities.commandv2.language.type.impl.StringParameterType;
import me.illusion.cosmos.utilities.commandv2.language.type.impl.filtered.TimeParameterType;
import me.illusion.cosmos.utilities.commandv2.language.type.impl.smart.ConditionParameterType;
import me.illusion.cosmos.utilities.commandv2.language.unit.MinecraftTime;

public class ParameterTypes {

    public static final ParameterType<String> STRING = new StringParameterType();
    public static final ParameterType<Integer> INTEGER = new IntegerParameterType();
    public static final ParameterType<Double> NUMERICAL = new NumericalParameterType();
    public static final ParameterType<MinecraftTime> MINECRAFT_TIME = new TimeParameterType();
    public static final ParameterType<String> CONDITION = new ConditionParameterType();

}
