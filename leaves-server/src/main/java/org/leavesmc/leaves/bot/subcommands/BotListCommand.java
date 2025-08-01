package org.leavesmc.leaves.bot.subcommands;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.command.LeavesSuggestionBuilder;
import org.leavesmc.leaves.entity.bot.Bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.Component.text;

public class BotListCommand implements LeavesSubcommand {

    @NotNull
    private static String formatPlayerNameList(@NotNull List<String> list) {
        if (list.isEmpty()) {
            return "";
        }
        String string = list.toString();
        return string.substring(1, string.length() - 1);
    }

    @Override
    public void execute(CommandSender sender, String subCommand, String[] args) {
        BotList botList = BotList.INSTANCE;
        if (args.length < 2) {
            Map<World, List<String>> botMap = new HashMap<>();
            for (World world : Bukkit.getWorlds()) {
                botMap.put(world, new ArrayList<>());
            }

            for (ServerBot bot : botList.bots) {
                Bot bukkitBot = bot.getBukkitEntity();
                botMap.get(bukkitBot.getWorld()).add(bukkitBot.getName());
            }

            sender.sendMessage("Total number: (" + botList.bots.size() + "/" + LeavesConfig.modify.fakeplayer.limit + ")");
            for (World world : botMap.keySet()) {
                sender.sendMessage(world.getName() + "(" + botMap.get(world).size() + "): " + formatPlayerNameList(botMap.get(world)));
            }
        } else {
            World world = Bukkit.getWorld(args[1]);

            if (world == null) {
                sender.sendMessage(text("Unknown world", NamedTextColor.RED));
                return;
            }

            List<String> snowBotList = new ArrayList<>();
            for (ServerBot bot : botList.bots) {
                Bot bukkitBot = bot.getBukkitEntity();
                if (bukkitBot.getWorld() == world) {
                    snowBotList.add(bukkitBot.getName());
                }
            }

            sender.sendMessage(world.getName() + "(" + botList.bots.size() + "): " + formatPlayerNameList(snowBotList));
        }
    }

    @Override
    public void suggest(@NotNull CommandSender sender, @NotNull String alias, @NotNull String @NotNull [] args, @Nullable Location location, LeavesSuggestionBuilder builder) throws IllegalArgumentException {
        Bukkit.getWorlds().forEach(world -> builder.suggest(world.getName()));
    }
}
