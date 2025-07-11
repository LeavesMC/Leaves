package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface UseItemAutoOffhandAction extends TimerBotAction<UseItemAutoOffhandAction> {
    static UseItemAutoOffhandAction create() {
        return Bukkit.getBotManager().newAction(UseItemAutoOffhandAction.class);
    }
}
