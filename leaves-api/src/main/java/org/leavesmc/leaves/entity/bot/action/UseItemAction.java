package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface UseItemAction extends TimerBotAction<UseItemAction> {
    static UseItemAction create() {
        return Bukkit.getBotManager().newAction(UseItemAction.class);
    }
}
