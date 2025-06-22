package org.leavesmc.leaves.bot.subcommands;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.AbstractBotConfig;
import org.leavesmc.leaves.bot.agent.Configs;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.command.LeavesSuggestionBuilder;
import org.leavesmc.leaves.event.bot.BotConfigModifyEvent;

import java.util.List;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;

public class BotConfigCommand implements LeavesSubcommand {

    @Override
    public void execute(CommandSender sender, String subCommand, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(text("Use /bot config <name> <config> to modify fakeplayer's config", NamedTextColor.RED));
            return;
        }

        ServerBot bot = BotList.INSTANCE.getBotByName(args[0]);
        if (bot == null) {
            sender.sendMessage(text("This fakeplayer is not in server", NamedTextColor.RED));
            return;
        }

        if (!Configs.getConfigNames().contains(args[1])) {
            sender.sendMessage(text("This config is not accept", NamedTextColor.RED));
            return;
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
                return;
            }
            CommandArgumentResult result = config.getArgument().parse(0, realArgs);

            try {
                config.setFromCommand(result);
                config.getChangeMessage().forEach(sender::sendMessage);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(text(e.getMessage(), NamedTextColor.RED));
            }
        }
    }


    @Override
    public void suggest(@NotNull CommandSender sender, @NotNull String alias, @NotNull String @NotNull [] args, @Nullable Location location, LeavesSuggestionBuilder builder) throws IllegalArgumentException {
        BotList botList = BotList.INSTANCE;
        ServerBot serverBot = null;

        if (args.length > 1 && (serverBot = botList.getBotByName(args[0])) == null) {
            builder.suggest("<" + args[0] + " not found>");
            return;
        }

        switch (args.length) {
            case 0, 1 -> botList.bots.forEach(bot -> builder.suggest(bot.getName().getString()));
            case 2 -> Configs.getConfigNames().forEach(builder::suggest);
            case 3, 4 -> {
                Configs<?> config = Configs.getConfig(args[1]);
                if (config == null) {
                    return;
                }
                AbstractBotConfig<?> botConfig = serverBot.getConfig(config);
                Pair<List<String>, String> results = botConfig.getArgument().suggestion(args.length - 3, sender, args[args.length - 1]);
                if (results == null || results.getLeft() == null) {
                    return;
                }

                for (String s : results.getLeft()) {
                    if (results.getRight() != null) {
                        builder.suggest(s, Component.literal(results.getRight()));
                    } else {
                        builder.suggest(s);
                    }
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return LeavesConfig.modify.fakeplayer.canModifyConfig;
    }
}
