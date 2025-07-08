package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.entity.Player;

public interface CraftCustomAction<E> {
    E createCraft(Player player, String[] args);

    E createEmptyCraft();
}
