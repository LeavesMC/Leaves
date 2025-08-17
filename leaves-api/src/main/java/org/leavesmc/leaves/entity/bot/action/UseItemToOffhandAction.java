package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot uses an item in its offhand to an entity.
 */
public interface UseItemToOffhandAction extends TimerBotAction<UseItemToOffhandAction> {

    /**
     * Gets the equivalent right-click hold duration in ticks.
     * Default is -1, which means will not be released.
     *
     * @return the equivalent right-click hold duration
     */
    int getUseTickTimeout();

    /**
     * Sets the equivalent right-click hold duration in ticks.
     *
     * @param timeout the equivalent right-click hold duration
     * @return this action instance
     */
    UseItemToOffhandAction setUseTickTimeout(int timeout);

    static UseItemToOffhandAction create() {
        return Bukkit.getBotManager().newAction(UseItemToOffhandAction.class);
    }
}
