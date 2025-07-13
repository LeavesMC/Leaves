package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot uses an item on a block.
 */
public interface UseItemOnAction extends TimerBotAction<UseItemOnAction> {
    static UseItemOnAction create() {
        return Bukkit.getBotManager().newAction(UseItemOnAction.class);
    }
}
