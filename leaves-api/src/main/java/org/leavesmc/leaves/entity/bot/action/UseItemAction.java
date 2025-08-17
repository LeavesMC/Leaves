package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot only uses an item, without using it on blocks or entities.
 */
public interface UseItemAction extends TimerBotAction<UseItemAction> {

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
    UseItemAction setUseTickTimeout(int timeout);

    static UseItemAction create() {
        return Bukkit.getBotManager().newAction(UseItemAction.class);
    }
}
