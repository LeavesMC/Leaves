package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action for a bot to perform a jump.
 */
public interface JumpAction extends TimerBotAction<JumpAction> {
    static JumpAction create() {
        return Bukkit.getBotManager().newAction(JumpAction.class);
    }
}
