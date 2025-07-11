package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface BreakBlockAction extends TimerBotAction<BreakBlockAction> {
    static BreakBlockAction create() {
        return Bukkit.getBotManager().newAction(BreakBlockAction.class);
    }
}
