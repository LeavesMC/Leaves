package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface AttackAction extends TimerBotAction<AttackAction> {
    static AttackAction create() {
        return Bukkit.getBotManager().newAction(AttackAction.class);
    }
}
