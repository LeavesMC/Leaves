package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot uses an item in its offhand on a block.
 */
public interface UseItemOnOffhandAction extends TimerBotAction<UseItemOnOffhandAction> {

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
    UseItemOnOffhandAction setUseTickTimeout(int timeout);

    static UseItemOnOffhandAction create() {
        return Bukkit.getBotManager().newAction(UseItemOnOffhandAction.class);
    }
}
