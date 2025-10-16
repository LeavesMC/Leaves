package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.AbstractTimerBotAction;
import org.leavesmc.leaves.entity.bot.action.TimerBotAction;

import java.util.function.Function;

public class CraftTimerBotAction<T extends TimerBotAction<T>, S extends AbstractTimerBotAction<S>> extends CraftBotAction<T, S> implements TimerBotAction<T> {

    public CraftTimerBotAction(S serverAction, Function<S, T> creator) {
        super(serverAction, creator);
    }

    @Override
    public void setStartDelayTick(int delayTick) {
        serverAction.setStartDelayTick(delayTick);
    }

    @Override
    public int getStartDelayTick() {
        return serverAction.getStartDelayTick();
    }

    @Override
    public void setDoIntervalTick(int intervalTick) {
        serverAction.setDoIntervalTick(intervalTick);
    }

    @Override
    public int getDoIntervalTick() {
        return serverAction.getDoIntervalTick();
    }

    @Override
    public void setDoNumber(int doNumber) {
        serverAction.setDoNumber(doNumber);
    }

    @Override
    public int getDoNumber() {
        return serverAction.getDoNumber();
    }

    @Override
    public int getTickToNext() {
        return serverAction.getTickToNext();
    }

    @Override
    public int getDoNumberRemaining() {
        return serverAction.getDoNumberRemaining();
    }
}