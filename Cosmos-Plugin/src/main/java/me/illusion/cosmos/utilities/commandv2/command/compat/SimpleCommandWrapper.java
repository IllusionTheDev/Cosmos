package me.illusion.cosmos.utilities.commandv2.command.compat;

import me.illusion.cosmos.utilities.command.SimpleCommand;
import me.illusion.cosmos.utilities.commandv2.command.compat.SimpleCommandWrapper.SimpleCommandExecutor;
import me.illusion.cosmos.utilities.commandv2.language.AbstractObjectiveModel;
import me.illusion.cosmos.utilities.commandv2.language.CompiledObjective;
import me.illusion.cosmos.utilities.commandv2.language.data.ObjectiveMetadata;
import me.illusion.cosmos.utilities.commandv2.language.type.Parameter;
import me.illusion.cosmos.utilities.commandv2.language.type.ParameterTypes;
import org.bukkit.command.CommandSender;

public class SimpleCommandWrapper extends AbstractObjectiveModel<SimpleCommandExecutor> {

    private final SimpleCommand command;
    private final int paramCount;

    public SimpleCommandWrapper(SimpleCommand command) {
        super(getSyntax(command));

        this.command = command;

        int paramIndex = 1;
        for (String sub : command.getIdentifier().split(".")) {
            if (sub.equalsIgnoreCase("*")) {
                registerParameter(new Parameter<>("param" + paramIndex++, ParameterTypes.STRING, true));
            }
        }

        paramCount = paramIndex - 1;
    }

    private static String getSyntax(SimpleCommand command) {
        StringBuilder builder = new StringBuilder();
        int paramIndex = 1;

        String[] split = command.getIdentifier().split(".");

        for (String sub : split) {
            if (sub.equalsIgnoreCase("*")) {
                builder.append("<param").append(paramIndex).append("> ");
            } else {
                builder.append(sub).append(" ");
            }
        }

        return builder.toString();
    }

    @Override
    public SimpleCommandExecutor compile(ObjectiveMetadata metadata) {
        return new SimpleCommandExecutor(metadata);
    }

    public class SimpleCommandExecutor extends CompiledObjective {

        public SimpleCommandExecutor(ObjectiveMetadata metadata) {
            super(metadata);
        }

        @Override
        public void execute(CommandSender sender) {
            String[] args = new String[paramCount];

            for (int index = 0; index < paramCount; index++) {
                args[index] = getParameter("param" + (index + 1));
            }

            command.execute(sender, args);
        }
    }
}
