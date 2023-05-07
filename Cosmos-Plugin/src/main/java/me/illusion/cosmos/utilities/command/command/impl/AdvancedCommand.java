package me.illusion.cosmos.utilities.command.command.impl;

import me.illusion.cosmos.utilities.command.command.impl.AdvancedCommand.AdvancedExecution;
import me.illusion.cosmos.utilities.command.language.AbstractObjectiveModel;
import me.illusion.cosmos.utilities.command.language.data.ObjectiveMetadata;
import org.bukkit.command.CommandSender;

public abstract class AdvancedCommand extends AbstractObjectiveModel<AdvancedExecution> {

    public AdvancedCommand(String syntax) {
        super(syntax);
    }

    public abstract void execute(CommandSender sender, ExecutionContext context);

    @Override
    public final AdvancedExecution compile(ObjectiveMetadata metadata) {
        return new AdvancedExecution(metadata);
    }

    class AdvancedExecution extends ExecutionContext {

        public AdvancedExecution(ObjectiveMetadata metadata) {
            super(metadata);
        }

        @Override
        public void execute(CommandSender sender) {
            AdvancedCommand.this.execute(sender, this);
        }
    }


}
