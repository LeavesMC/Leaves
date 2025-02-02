package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.agent.BotAction;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractTimerAction<E extends AbstractTimerAction<E>> extends BotAction<E> {

    public AbstractTimerAction(String name, Supplier<E> creator) {
        super(name, CommandArgument.of(CommandArgumentType.INTEGER, CommandArgumentType.INTEGER), creator);
        this.setTabComplete(0, List.of("[TickDelay]")).setTabComplete(1, List.of("[DoNumber]"));
    }

    @Override
    public void loadCommand(@Nullable ServerPlayer player, @NotNull CommandArgumentResult result) {
        this.setTickDelay(result.readInt(20)).setNumber(result.readInt(-1));
    }
}
