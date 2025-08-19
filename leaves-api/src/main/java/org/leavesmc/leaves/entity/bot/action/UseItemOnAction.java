package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot uses an item on a block.
 */
public interface UseItemOnAction extends TimerBotAction<UseItemOnAction> {

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
    UseItemOnAction setUseTickTimeout(int timeout);

    static UseItemOnAction create() {
        return Bukkit.getBotManager().newAction(UseItemOnAction.class);
    }
}
