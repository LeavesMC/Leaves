package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface JumpAction extends TimerBotAction<JumpAction> {
    static JumpAction create() {
        return Bukkit.getBotManager().newAction(JumpAction.class);
    }
}
