package org.leavesmc.leaves.bot.subcommands;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.bot.BotCreateState;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.BotUtil;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.command.LeavesSuggestionBuilder;
import org.leavesmc.leaves.event.bot.BotCreateEvent;

import static net.kyori.adventure.text.Component.text;

public class BotCreateCommand implements LeavesSubcommand {

    @Override
    public void execute(CommandSender sender, String subCommand, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(text("Use /bot create <name> [skin_name] to create a fakeplayer", NamedTextColor.RED));
            return;
        }

        String botName = args[0];
        String fullName = BotUtil.getFullName(botName);
        if (this.canCreate(sender, fullName)) {
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
    }

    @Override
    public void suggest(@NotNull CommandSender sender, @NotNull String alias, @NotNull String @NotNull [] args, @Nullable Location location, LeavesSuggestionBuilder builder) throws IllegalArgumentException {
        if (args.length <= 1) {
            builder.suggest("<BotName>");
        }
        if (args.length == 2) {
            builder.suggest("[SkinName]");
        }
        if (sender instanceof ConsoleCommandSender && args.length == 3) {
            Bukkit.getWorlds().forEach(world -> builder.suggest(world.getName()));
        }
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
