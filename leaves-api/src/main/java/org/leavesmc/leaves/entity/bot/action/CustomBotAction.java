package org.leavesmc.leaves.entity.bot.action;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.command.LeavesWrappedArgument;
import org.leavesmc.leaves.entity.bot.Bot;
import org.leavesmc.leaves.util.ExtraData;

@SuppressWarnings("unused")
public abstract class CustomBotAction {
    private LeavesWrappedArgument.ArgumentHandler factory;

    public abstract boolean doTick(@NotNull Bot bot);

    public abstract String getName();

    public void init() {
    }

    public void loadCommand(CommandContext<CommandSourceStack> context) {
    }

    public void provideActionData(@NotNull ExtraData data) {
    }

    public void provideArgumentFactory(LeavesWrappedArgument.ArgumentHandler factory) {
        this.factory = factory;
    }

    protected <T> LeavesWrappedArgument<T> addArgument(String name, ArgumentType<T> type) {
        if (factory == null) {
            throw new IllegalStateException("Argument factory not provided! Are you calling addArgument() outside of init() method?");
        }
        return factory.create(name, type);
    }

    protected void fork(int forkId) {
        if (factory == null) {
            throw new IllegalStateException("Argument factory not provided! Are you calling fork() outside of init() method?");
        }
        factory.fork(forkId);
    }

    @ApiStatus.Internal
    public interface InternalCustomBotAction extends BotAction<InternalCustomBotAction> {

    }
}
