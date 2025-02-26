package org.leavesmc.leaves.bot.subcommands;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.command.LeavesSubcommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class BotLoadCommand implements LeavesSubcommand {
    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        if (!LeavesConfig.modify.fakeplayer.canManualSaveAndLoad) {
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage(text("Use /bot save <name> to save a fakeplayer", NamedTextColor.RED));
            return false;
        }

        String realName = args[1];
        BotList botList = BotList.INSTANCE;
        if (!botList.getSavedBotList().contains(realName)) {
            sender.sendMessage(text("This fakeplayer is not saved", NamedTextColor.RED));
            return false;
        }

        if (botList.loadNewBot(realName) == null) {
            sender.sendMessage(text("Can't load bot, please check", NamedTextColor.RED));
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String[] args, Location location) {
        if (!LeavesConfig.modify.fakeplayer.canManualSaveAndLoad) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>();
        BotList botList = BotList.INSTANCE;

        if (args.length <= 1) {
            list.addAll(botList.bots.stream().map(e -> e.getName().getString()).toList());
        }

        return list;
    }

    @Override
    public boolean tabCompletes() {
        return LeavesConfig.modify.fakeplayer.canManualSaveAndLoad;
    }
}
