package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.bot.action.TimerBotAction;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public abstract class CraftTimerBotAction<E extends TimerBotAction<E>> extends CraftBotAction<E> implements TimerBotAction<E> {

    public CraftTimerBotAction(String name, Supplier<E> creator) {
        this(name, CommandArgument.of(CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER), creator);
    }

    public CraftTimerBotAction(String name, CommandArgument argument, Supplier<E> creator) {
        super(name, argument, creator);
        this.setSuggestion(0, Pair.of(Collections.singletonList("0"), "[TickDelay]"));
        this.setSuggestion(1, Pair.of(Collections.singletonList("20"), "[TickInterval]"));
        this.setSuggestion(2, Pair.of(List.of("1", "-1"), "[DoNumber]"));
    }

    @Override
    public void loadCommand(ServerPlayer player, @NotNull CommandArgumentResult result) {
        this.setStartDelayTick0(result.readInt(0));
        this.setDoIntervalTick0(result.readInt(20));
        this.setDoNumber0(result.readInt(1));
    }

    @Override
    public int getDoNumberRemaining() {
        return super.getDoNumberRemaining();
    }

    @Override
    public int getTickToNext() {
        return super.getTickToNext();
    }

    @Override
    public int getDoNumber() {
        return super.getDoNumber();
    }

    @Override
    public void setDoNumber(int initialNumber) {
        super.setDoNumber0(initialNumber);
    }

    @Override
    public int getDoIntervalTick() {
        return super.getDoIntervalTick();
    }

    @Override
    public void setDoIntervalTick(int initialTickInterval) {
        super.setDoIntervalTick0(initialTickInterval);
    }

    @Override
    public int getStartDelayTick() {
        return super.getStartDelayTick();
    }

    @Override
    public void setStartDelayTick(int initialTickDelay) {
        super.setStartDelayTick0(initialTickDelay);
    }
}
