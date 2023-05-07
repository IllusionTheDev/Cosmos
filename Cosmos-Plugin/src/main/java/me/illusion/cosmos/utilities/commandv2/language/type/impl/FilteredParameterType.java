package me.illusion.cosmos.utilities.commandv2.language.type.impl;

import java.util.List;
import me.illusion.cosmos.utilities.commandv2.language.type.ParameterType;
import me.illusion.cosmos.utilities.text.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface FilteredParameterType<Type> extends ParameterType<Type> {

    List<Type> getAllValues();

    @Nullable
    List<Placeholder<Player>> createPlaceholders(Object value);

}
