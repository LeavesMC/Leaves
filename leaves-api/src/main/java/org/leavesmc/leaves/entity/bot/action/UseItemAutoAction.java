package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface UseItemAutoAction extends TimerBotAction<UseItemAutoAction> {
    static UseItemAutoAction create() {
        return Bukkit.getBotManager().newAction(UseItemAutoAction.class);
    }
}
