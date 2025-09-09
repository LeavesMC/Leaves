package org.leavesmc.leaves.bot.subcommands;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.command.LeavesSuggestionBuilder;

import static net.kyori.adventure.text.Component.text;

public class BotLoadCommand implements LeavesSubcommand {

    @Override
    public void execute(CommandSender sender, String subCommand, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(text("Use /bot load <name> to save a fakeplayer", NamedTextColor.RED));
            return;
        }

        String realName = args[0];
        BotList botList = BotList.INSTANCE;
        if (!botList.getOfflineSavedBotList().contains(realName)) {
            sender.sendMessage(text("This fakeplayer is not saved or still online", NamedTextColor.RED));
            return;
        }

        if (botList.loadNewBot(realName) == null) {
            sender.sendMessage(text("Can't load bot, please check", NamedTextColor.RED));
        }
    }

    @Override
    public void suggest(@NotNull CommandSender sender, @NotNull String alias, @NotNull String @NotNull [] args, @Nullable Location location, LeavesSuggestionBuilder builder) throws IllegalArgumentException {
        BotList botList = BotList.INSTANCE;
        if (args.length <= 1) {
            botList.getOfflineSavedBotList().forEach(builder::suggest);
        }
    }

    @Override
    public boolean isEnabled() {
        return LeavesConfig.modify.fakeplayer.canManualSaveAndLoad;
    }
}
