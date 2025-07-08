package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface CraftCustomAction<E> {
    public E createCraft(@Nullable Player player, String[] args);
}
