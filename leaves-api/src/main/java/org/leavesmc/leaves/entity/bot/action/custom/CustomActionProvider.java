package org.leavesmc.leaves.entity.bot.action.custom;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.leavesmc.leaves.entity.bot.Bot;

import java.util.List;

// I don't know how to name it, meow~
public interface CustomActionProvider {

    String id();

    Plugin provider();

    boolean doTick(Bot bot);

    List<String> getSuggestion(CommandSender sender, String[] args);

    void loadAction(CommandSender sender, String[] args, CustomAction action);
}
