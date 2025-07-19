package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action for a bot to break a block.
 */
public interface BreakBlockAction extends TimerBotAction<BreakBlockAction> {
    static BreakBlockAction create() {
        return Bukkit.getBotManager().newAction(BreakBlockAction.class);
    }
}
