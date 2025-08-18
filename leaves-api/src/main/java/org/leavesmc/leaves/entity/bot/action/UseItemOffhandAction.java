package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot only uses the item in its offhand, without using it on blocks or entities.
 */
public interface UseItemOffhandAction extends TimerBotAction<UseItemOffhandAction> {

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
    UseItemOffhandAction setUseTickTimeout(int timeout);

    static UseItemOffhandAction create() {
        return Bukkit.getBotManager().newAction(UseItemOffhandAction.class);
    }
}
