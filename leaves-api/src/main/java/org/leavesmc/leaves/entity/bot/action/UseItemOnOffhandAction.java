package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot uses an item in its offhand on a block.
 */
public interface UseItemOnOffhandAction extends TimerBotAction<UseItemOnOffhandAction> {
    static UseItemOnOffhandAction create() {
        return Bukkit.getBotManager().newAction(UseItemOnOffhandAction.class);
    }
}
