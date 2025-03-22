package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.agent.AbstractBotAction;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.util.Collections;
import java.util.function.Supplier;

public abstract class AbstractTimerAction<E extends AbstractTimerAction<E>> extends AbstractBotAction<E> {

    public AbstractTimerAction(String name, Supplier<E> creator) {
        super(name, CommandArgument.of(CommandArgumentType.INTEGER, CommandArgumentType.INTEGER), creator);
        this.setTabComplete(0, Collections.singletonList("[TickDelay]")).setTabComplete(1, Collections.singletonList("[TickInterval]")).setTabComplete(2, Collections.singletonList("[DoNumber]"));
    }

    @Override
    public void loadCommand(@Nullable ServerPlayer player, @NotNull CommandArgumentResult result) {
        this.setInitialTickDelay(result.readInt(0)).setInitialTickInterval(result.readInt(20)).setInitialNumber(result.readInt(-1));
    }
}
