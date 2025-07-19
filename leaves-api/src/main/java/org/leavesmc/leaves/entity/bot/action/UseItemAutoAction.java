package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot uses an item, fully simulating the effect of a player right-clicking.
 */
public interface UseItemAutoAction extends TimerBotAction<UseItemAutoAction> {

    /**
     * Gets the equivalent right-click hold duration in ticks.
     * Default is -1, which means will not be released.
     *
     * @return the equivalent right-click hold duration
     */
    int getUseTick();

    /**
     * Sets the equivalent right-click hold duration in ticks.
     *
     * @param useTick the equivalent right-click hold duration
     * @return this action instance
     */
    UseItemAutoAction setUseTick(int useTick);

    static UseItemAutoAction create() {
        return Bukkit.getBotManager().newAction(UseItemAutoAction.class);
    }
}
