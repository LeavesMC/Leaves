package org.leavesmc.leaves.entity.bot.action.custom;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface BukkitLikeProcessor extends CommandProcessor {

    List<String> getSuggestion(CommandSender sender, String[] args);

    void loadAction(CommandSender sender, String[] args, CustomAction action);
}
