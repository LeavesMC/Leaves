package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot uses an item on a block.
 */
public interface UseItemOnAction extends TimerBotAction<UseItemOnAction> {

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
    UseItemOnAction setUseTick(int useTick);

    static UseItemOnAction create() {
        return Bukkit.getBotManager().newAction(UseItemOnAction.class);
    }
}
