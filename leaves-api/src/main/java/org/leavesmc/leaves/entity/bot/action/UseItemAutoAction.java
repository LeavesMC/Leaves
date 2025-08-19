package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot uses an item, fully simulating the effect of a player right-clicking.
 */
public interface UseItemAutoAction extends TimerBotAction<UseItemAutoAction> {

    /**
     * Gets the equivalent right-click hold duration timeout in ticks.
     * Default is -1, which means no timeout.
     *
     * @return the equivalent right-click hold duration timeout
     */
    int getUseTickTimeout();

    /**
     * Sets the equivalent right-click hold duration timeout in ticks.
     *
     * @param timeout the equivalent right-click hold duration timeout
     * @return this action instance
     */
    UseItemAutoAction setUseTickTimeout(int timeout);

    static UseItemAutoAction create() {
        return Bukkit.getBotManager().newAction(UseItemAutoAction.class);
    }
}
