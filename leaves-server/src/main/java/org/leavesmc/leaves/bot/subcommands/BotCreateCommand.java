package org.leavesmc.leaves.bot.subcommands;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.bot.BotCreateState;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.event.bot.BotCreateEvent;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class BotCreateCommand implements LeavesSubcommand {
    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(text("Use /bot create <name> [skin_name] to create a fakeplayer", NamedTextColor.RED));
            return false;
        }

        String botName = args[0];
        if (this.canCreate(sender, botName)) {
            BotCreateState.Builder builder = BotCreateState.builder(botName, Bukkit.getWorlds().getFirst().getSpawnLocation()).createReason(BotCreateEvent.CreateReason.COMMAND).creator(sender);

            if (args.length >= 2) {
                builder.skinName(args[1]);
            }

            if (sender instanceof Player player) {
                builder.location(player.getLocation());
            } else if (sender instanceof ConsoleCommandSender) {
                if (args.length >= 6) {
                    try {
                        World world = Bukkit.getWorld(args[2]);
                        double x = Double.parseDouble(args[3]);
                        double y = Double.parseDouble(args[4]);
                        double z = Double.parseDouble(args[5]);
                        if (world != null) {
                            builder.location(new Location(world, x, y, z));
                        }
                    } catch (Exception e) {
                        LeavesLogger.LOGGER.warning("Can't build location", e);
                    }
                }
            }

            builder.spawnWithSkin(null);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String[] args, Location location) {
        List<String> list = new ArrayList<>();
        if (args.length <= 1) {
            list.add("<BotName>");
        }
        if (args.length == 2) {
            list.add("[SkinName]");
        }
        String[] locStr = {
                String.valueOf(location.getBlockX()),
                String.valueOf(location.getBlockY()),
                String.valueOf(location.getBlockZ())
        };
        if (args.length == 3) {
            for (var world : sender.getServer().getWorlds()) {
                list.add(world.getName());
            }
        }
        if (args.length >= 4 && args.length <= 6) {
            list.add(locStr[args.length - 4]);
        }
        if (args.length == 4) {
            list.add(String.join(" ", locStr));
        }
        if (args.length == 5) {
            list.add(locStr[1] + " " + locStr[2]);
        }
        return list;
    }

    private boolean canCreate(CommandSender sender, @NotNull String name) {
        BotList botList = BotList.INSTANCE;
        if (!name.matches("^[a-zA-Z0-9_]{4,16}$")) {
            sender.sendMessage(text("This name is illegal", NamedTextColor.RED));
            return false;
        }

        if (Bukkit.getPlayerExact(name) != null || botList.getBotByName(name) != null) {
            sender.sendMessage(text("This player is in server", NamedTextColor.RED));
            return false;
        }

        if (LeavesConfig.modify.fakeplayer.unableNames.contains(name)) {
            sender.sendMessage(text("This name is not allowed", NamedTextColor.RED));
            return false;
        }

        if (botList.bots.size() >= LeavesConfig.modify.fakeplayer.limit) {
            sender.sendMessage(text("Fakeplayer limit is full", NamedTextColor.RED));
            return false;
        }

        return true;
    }
}
