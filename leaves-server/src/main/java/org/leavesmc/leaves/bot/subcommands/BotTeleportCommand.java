package org.leavesmc.leaves.bot.subcommands;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.event.bot.BotRemoveEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class BotTeleportCommand implements LeavesSubcommand {

    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage(text("Use /bot tp <name> to teleport a fakeplayer to your location", NamedTextColor.RED));
            return false;
        }

        BotList botList = BotList.INSTANCE;
        ServerBot bot = botList.getBotByName(args[0]);

        if (bot == null) {
            sender.sendMessage(text("This fakeplayer is not in server", NamedTextColor.RED));
            return false;
        }

        bot.teleportTo(player.getLocation().x(), player.getLocation().y(), player.getLocation().z());

        sender.sendMessage("Teleported " + bot.getScoreboardName() + " to your location");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String[] args, Location location) {
        List<String> list = new ArrayList<>();
        BotList botList = BotList.INSTANCE;

        if (args.length <= 1) {
            list.addAll(botList.bots.stream().map(e -> e.getName().getString()).toList());
        }

        return list;
    }
}
