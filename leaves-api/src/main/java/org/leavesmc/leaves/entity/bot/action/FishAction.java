package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface FishAction extends TimerBotAction<FishAction> {
    static FishAction create() {
        return Bukkit.getBotManager().newAction(FishAction.class);
    }
}
