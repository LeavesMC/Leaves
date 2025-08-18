package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot uses an item in its offhand to an entity.
 */
public interface UseItemToOffhandAction extends TimerBotAction<UseItemToOffhandAction> {

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
    UseItemToOffhandAction setUseTickTimeout(int timeout);

    static UseItemToOffhandAction create() {
        return Bukkit.getBotManager().newAction(UseItemToOffhandAction.class);
    }
}
