package org.leavesmc.leaves.bot.subcommands;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.command.LeavesSuggestionBuilder;
import org.leavesmc.leaves.event.bot.BotRemoveEvent;

import static net.kyori.adventure.text.Component.text;

public class BotSaveCommand implements LeavesSubcommand {

    @Override
    public void execute(CommandSender sender, String subCommand, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(text("Use /bot save <name> to save a fakeplayer", NamedTextColor.RED));
            return;
        }

        BotList botList = BotList.INSTANCE;
        ServerBot bot = botList.getBotByName(args[0]);

        if (bot == null) {
            sender.sendMessage(text("This fakeplayer is not in server", NamedTextColor.RED));
            return;
        }

        if (botList.removeBot(bot, BotRemoveEvent.RemoveReason.COMMAND, sender, true)) {
            sender.sendMessage(bot.getScoreboardName() + " saved to " + bot.createState.realName());
        }
    }

    @Override
    public void suggest(@NotNull CommandSender sender, @NotNull String alias, @NotNull String @NotNull [] args, @Nullable Location location, LeavesSuggestionBuilder builder) throws IllegalArgumentException {
        BotList botList = BotList.INSTANCE;
        if (args.length <= 1) {
            botList.bots.forEach(bot -> builder.suggest(bot.getName().getString()));
        }
    }

    @Override
    public boolean isEnabled() {
        return LeavesConfig.modify.fakeplayer.canManualSaveAndLoad;
    }
}
