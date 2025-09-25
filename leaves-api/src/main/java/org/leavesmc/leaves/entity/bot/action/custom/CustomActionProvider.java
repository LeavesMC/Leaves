package org.leavesmc.leaves.entity.bot.action.custom;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.leavesmc.leaves.entity.bot.Bot;

import java.util.List;

public interface CustomActionProvider {

    String id();

    Plugin provider();

    boolean doTick(Bot bot, CustomAction action);

    List<String> getSuggestion(CommandSender sender, String[] args);

    void loadAction(CommandSender sender, String[] args, CustomAction action);
}
