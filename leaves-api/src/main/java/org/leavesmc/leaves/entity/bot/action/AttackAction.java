package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action for a bot to attack entities.
 */
public interface AttackAction extends TimerBotAction<AttackAction> {
    static AttackAction create() {
        return Bukkit.getBotManager().newAction(AttackAction.class);
    }
}
