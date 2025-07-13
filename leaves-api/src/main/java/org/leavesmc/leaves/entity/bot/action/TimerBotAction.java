package org.leavesmc.leaves.entity.bot.action;

/**
 * Represents a scheduled bot task that runs periodically.
 * <p>
 * TimerBotAction allows configuration of start delay, execution interval, and the number of executions.
 * It is intended for bot actions that need to be triggered at regular intervals.
 *
 * @param <E> the type of entity this action operates on
 */
public interface TimerBotAction<E> extends BotAction<E> {

    /**
     * Sets the delay in ticks before the task starts for the first time.
     *
     * @param delayTick the number of ticks to delay before the first execution
     */
    void setStartDelayTick(int delayTick);

    /**
     * Gets the delay in ticks before the task starts for the first time.
     *
     * @return the number of ticks to delay before the first execution
     */
    int getStartDelayTick();

    /**
     * Sets the interval in ticks between each execution of the task.
     *
     * @param intervalTick the number of ticks between executions
     */
    void setDoIntervalTick(int intervalTick);

    /**
     * Gets the interval in ticks between each execution of the task.
     *
     * @return the number of ticks between executions
     */
    int getDoIntervalTick();

    /**
     * Sets the total number of times the task should be executed.
     *
     * @param doNumber the total number of executions
     */
    void setDoNumber(int doNumber);

    /**
     * Gets the total number of times the task should be executed.
     *
     * @return the total number of executions
     */
    int getDoNumber();

    /**
     * Gets the number of ticks remaining until the next execution.
     *
     * @return the number of ticks until the next execution
     */
    int getTickToNext();

    /**
     * Gets the number of executions remaining for this task.
     *
     * @return the number of executions remaining
     */
    int getDoNumberRemaining();
}
