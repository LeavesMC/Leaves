package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface UseItemOffhandAction extends TimerBotAction<UseItemOffhandAction> {
    static UseItemOffhandAction create() {
        return Bukkit.getBotManager().newAction(UseItemOffhandAction.class);
    }
}
