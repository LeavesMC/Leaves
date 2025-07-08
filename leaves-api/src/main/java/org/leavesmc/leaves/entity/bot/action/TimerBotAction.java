package org.leavesmc.leaves.entity.bot.action;

public interface TimerBotAction<E> extends BotAction<E> {

    void setStartDelayTick(int delayTick);

    int getStartDelayTick();

    void setDoIntervalTick(int intervalTick);

    int getDoIntervalTick();

    void setDoNumber(int doNumber);

    int getDoNumber();

    int getTickToNext();

    int getDoNumberRemaining();
}
