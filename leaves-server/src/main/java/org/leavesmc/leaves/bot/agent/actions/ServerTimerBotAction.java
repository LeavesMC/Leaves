package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.util.List;
import java.util.function.Supplier;

public abstract class ServerTimerBotAction<E extends ServerTimerBotAction<E>> extends ServerBotAction<E> {

    public ServerTimerBotAction(String name, Supplier<E> creator) {
        this(name, CommandArgument.of(CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER), creator);
    }

    public ServerTimerBotAction(String name, CommandArgument argument, Supplier<E> creator) {
        super(name, argument, creator);
        this.setSuggestion(0, Pair.of(List.of("0"), "[TickDelay]"));
        this.setSuggestion(1, Pair.of(List.of("20"), "[TickInterval]"));
        this.setSuggestion(2, Pair.of(List.of("1", "-1"), "[DoNumber]"));
    }

    @Override
    public void loadCommand(ServerPlayer player, @NotNull CommandArgumentResult result) {
        this.setStartDelayTick(result.readInt(0));
        this.setDoIntervalTick(result.readInt(20));
        this.setDoNumber(result.readInt(1));
    }
}
