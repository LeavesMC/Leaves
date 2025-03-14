package org.leavesmc.leaves.bot.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.event.bot.BotRemoveEvent;
import org.leavesmc.leaves.plugin.MinecraftInternalPlugin;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class BotRemoveCommand implements LeavesSubcommand {

    private final Component errorMessage = text("Usage: /bot remove <name> [hour] [minute] [second]", NamedTextColor.RED);

    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        if (args.length < 1 || args.length > 4) {
            sender.sendMessage(errorMessage);
            return false;
        }

        BotList botList = BotList.INSTANCE;
        ServerBot bot = botList.getBotByName(args[0]);

        if (bot == null) {
            sender.sendMessage(text("This fakeplayer is not in server", NamedTextColor.RED));
            return false;
        }

        if (args.length == 2 && args[1].equals("cancel")) {
            if (bot.removeTaskId == -1) {
                sender.sendMessage(text("This fakeplayer is not scheduled to be removed", NamedTextColor.RED));
                return false;
            }
            Bukkit.getScheduler().cancelTask(bot.removeTaskId);
            bot.removeTaskId = -1;
            sender.sendMessage(text("Remove cancel"));
            return false;
        }

        if (args.length > 1) {
            long time = 0;
            int h; // Preventing out-of-range
            long s = 0;
            long m = 0;

            try {
                h = Integer.parseInt(args[1]);
                if (h < 0) {
                    throw new NumberFormatException();
                }
                time += ((long) h) * 3600 * 20;
                if (args.length > 2) {
                    m = Long.parseLong(args[2]);
                    if (m > 59 || m < 0) {
                        throw new NumberFormatException();
                    }
                    time += m * 60 * 20;
                }
                if (args.length > 3) {
                    s = Long.parseLong(args[3]);
                    if (s > 59 || s < 0) {
                        throw new NumberFormatException();
                    }
                    time += s * 20;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(errorMessage);
                return false;
            }

            boolean isReschedule = bot.removeTaskId != -1;

            if (isReschedule) {
                Bukkit.getScheduler().cancelTask(bot.removeTaskId);
            }
            bot.removeTaskId = Bukkit.getScheduler().runTaskLater(MinecraftInternalPlugin.INSTANCE, () -> {
                bot.removeTaskId = -1;
                botList.removeBot(bot, BotRemoveEvent.RemoveReason.COMMAND, sender, false);
            }, time).getTaskId();

            sender.sendMessage("This fakeplayer will be removed in " + h + "h " + m + "m " + s + "s" + (isReschedule ? " (rescheduled)" : ""));

            return false;
        }

        botList.removeBot(bot, BotRemoveEvent.RemoveReason.COMMAND, sender, false);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String[] args, Location location) {
        List<String> list = new ArrayList<>();
        BotList botList = BotList.INSTANCE;

        if (args.length <= 1) {
            list.addAll(botList.bots.stream().map(e -> e.getName().getString()).toList());
        }

        if (args.length == 2) {
            list.add("cancel");
            list.add("[hour]");
        }

        if (args.length > 2 && !args[1].equals("cancel")) {
            switch (args.length) {
                case 3 -> list.add("[minute]");
                case 4 -> list.add("[second]");
            }
        }


        return list;
    }
}
