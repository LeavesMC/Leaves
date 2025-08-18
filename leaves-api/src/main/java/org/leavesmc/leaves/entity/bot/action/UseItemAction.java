package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot only uses an item, without using it on blocks or entities.
 */
public interface UseItemAction extends TimerBotAction<UseItemAction> {

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
    UseItemAction setUseTickTimeout(int timeout);

    static UseItemAction create() {
        return Bukkit.getBotManager().newAction(UseItemAction.class);
    }
}
