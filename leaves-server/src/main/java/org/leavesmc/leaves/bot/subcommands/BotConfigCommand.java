package org.leavesmc.leaves.bot.subcommands;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.AbstractBotConfig;
import org.leavesmc.leaves.bot.agent.Configs;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.event.bot.BotConfigModifyEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;

public class BotConfigCommand implements LeavesSubcommand {

    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        if (!LeavesConfig.modify.fakeplayer.canModifyConfig) {
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage(text("Use /bot config <name> <config> to modify fakeplayer's config", NamedTextColor.RED));
            return false;
        }

        ServerBot bot = BotList.INSTANCE.getBotByName(args[0]);
        if (bot == null) {
            sender.sendMessage(text("This fakeplayer is not in server", NamedTextColor.RED));
            return false;
        }

        if (!Configs.getConfigNames().contains(args[1])) {
            sender.sendMessage(text("This config is not accept", NamedTextColor.RED));
            return false;
        }

        AbstractBotConfig<?> config = bot.getConfig(Objects.requireNonNull(Configs.getConfig(args[1])));
        if (args.length < 3) {
            config.getMessage().forEach(sender::sendMessage);
        } else {
            String[] realArgs = new String[args.length - 2];
            System.arraycopy(args, 2, realArgs, 0, realArgs.length);

            BotConfigModifyEvent event = new BotConfigModifyEvent(bot.getBukkitEntity(), config.getName(), realArgs, sender);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return false;
            }
            CommandArgumentResult result = config.getArgument().parse(0, realArgs);

            try {
                config.setFromCommand(result);
                config.getChangeMessage().forEach(sender::sendMessage);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(text(e.getMessage(), NamedTextColor.RED));
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String[] args, Location location) {
        if (!LeavesConfig.modify.fakeplayer.canModifyConfig) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>();
        BotList botList = BotList.INSTANCE;

        if (args.length <= 1) {
            list.addAll(botList.bots.stream().map(e -> e.getName().getString()).toList());
        } else {
            ServerBot bot = botList.getBotByName(args[0]);
            if (bot == null) {
                return Collections.singletonList("<" + args[0] + " not found>");
            } else {
                if (args.length == 2) {
                    list.addAll(Configs.getConfigNames());
                }

                if (args.length >= 3) {
                    return Collections.singletonList("<" + args[1] + " not found>");
                }
            }
        }

        return list;
    }

    @Override
    public CompletableFuture<Suggestions> tabSuggestion(CommandSender sender, String subCommand, String[] args, Location location, SuggestionsBuilder builder) {
        if (!LeavesConfig.modify.fakeplayer.canModifyConfig) {
            return null;
        }

        if (args.length >= 3) {
            ServerBot bot = BotList.INSTANCE.getBotByName(args[0]);
            if (bot == null) {
                return null;
            }

            Configs<?> config = Configs.getConfig(args[1]);
            if (config != null) {
                AbstractBotConfig<?> botConfig = bot.getConfig(config);
                Pair<List<String>, String> results = botConfig.getArgument().suggestion(args.length - 3, sender, args[args.length - 1]);

                if (results == null || results.getLeft() == null) {
                    return builder.buildFuture();
                }

                builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + 1);
                for (String s : results.getLeft()) {
                    if (results.getRight() != null) {
                        builder.suggest(s, Component.literal(results.getRight()));
                    } else {
                        builder.suggest(s);
                    }
                }

                return builder.buildFuture();
            }
        }

        return null;
    }

    @Override
    public boolean tabCompletes() {
        return LeavesConfig.modify.fakeplayer.canModifyConfig;
    }
}
