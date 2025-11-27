package org.leavesmc.leaves.command.bot.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.adventure.PaperAdventure;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.ArgumentNode;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.arguments.BotArgumentType;
import org.leavesmc.leaves.command.bot.BotSubcommand;
import org.leavesmc.leaves.event.bot.BotRemoveEvent;
import org.leavesmc.leaves.plugin.MinecraftInternalPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.spaces;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.minecraft.network.chat.Component.literal;

public class RemoveCommand extends BotSubcommand {

    public RemoveCommand() {
        super("remove");
        children(BotArgument::new);
    }

    private static boolean removeBot(@NotNull ServerBot bot, @Nullable CommandSender sender) {
        boolean success = BotList.INSTANCE.removeBot(bot, BotRemoveEvent.RemoveReason.COMMAND, sender, false, false);
        if (!success) {
            sender = sender == null ? Bukkit.getConsoleSender() : sender;
            sender.sendMessage(text("Bot remove canceled by a plugin", RED));
        }
        return success;
    }

    private static class BotArgument extends ArgumentNode<ServerBot> {
        private BotArgument() {
            super("bot", BotArgumentType.bot());
            children(RemoveTimeArgument::new);
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) {
            ServerBot bot = context.getArgument(BotArgument.class);
            return removeBot(bot, context.getSender());
        }
    }

    private static class RemoveTimeArgument extends ArgumentNode<String> {

        private RemoveTimeArgument() {
            super("remove_time", StringArgumentType.word());
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) throws CommandSyntaxException {
            String removeTimeStr = context.getArgument("remove_time", String.class);
            int removeTimeSeconds = parseRemoveTime(removeTimeStr);
            ServerBot bot = context.getArgument(BotArgument.class);
            CommandSender sender = context.getSender();

            boolean isReschedule = bot.removeTaskId != -1;

            if (isReschedule) {
                Bukkit.getScheduler().cancelTask(bot.removeTaskId);
            }
            bot.removeTaskId = Bukkit.getScheduler().runTaskLater(MinecraftInternalPlugin.INSTANCE, () -> {
                bot.removeTaskId = -1;
                removeBot(bot, sender);
            }, removeTimeSeconds * 20L).getTaskId();

            sender.sendMessage(join(spaces(),
                text("Bot", GRAY),
                PaperAdventure.asAdventure(bot.getDisplayName()),
                text("scheduled for removal in", GRAY),
                text(formatSeconds(removeTimeSeconds), AQUA),
                text(isReschedule ? "(rescheduled)" : "", GRAY)
            ));
            return true;
        }

        private static int parseRemoveTime(String timeStr) throws CommandSyntaxException {
            if (timeStr == null || timeStr.trim().isEmpty()) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
            }

            if (!timeStr.matches("^[\\d\\shmsHMS]+$")) {
                throw new CommandSyntaxException(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                    literal("Invalid time format: " + timeStr)
                );
            }

            String remaining = timeStr.replaceAll("\\d+[hmsHMS]", "").trim();
            if (!remaining.isEmpty() && remaining.matches(".*\\d+.*")) {
                throw new CommandSyntaxException(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                    literal("Found trailing numbers without unit: " + timeStr)
                );
            }

            Matcher matcher = Pattern.compile("(\\d+)([hmsHMS])").matcher(timeStr);
            long seconds = 0;
            boolean foundMatch = false;

            while (matcher.find()) {
                foundMatch = true;
                long value;
                try {
                    value = Long.parseLong(matcher.group(1));
                } catch (NumberFormatException e) {
                    throw new CommandSyntaxException(
                        CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                        literal("Number too large: " + matcher.group(1))
                    );
                }

                switch (matcher.group(2).toLowerCase()) {
                    case "h" -> seconds += value * 3600;
                    case "m" -> seconds += value * 60;
                    case "s" -> seconds += value;
                }
            }

            if (!foundMatch) {
                throw new CommandSyntaxException(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                    literal("No valid time units found in: " + timeStr)
                );
            }

            if (seconds > Integer.MAX_VALUE) {
                throw new CommandSyntaxException(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                    literal("Total time exceeds maximum limit")
                );
            }

            return (int) seconds;
        }

        private static @NotNull String formatSeconds(int totalSeconds) {
            int h = totalSeconds / 3600;
            int m = (totalSeconds % 3600) / 60;
            int s = totalSeconds % 60;
            StringBuilder sb = new StringBuilder();
            if (h > 0) {
                sb.append(h).append("h");
            }
            if (m > 0) {
                sb.append(m).append("m");
            }
            if (s > 0) {
                sb.append(s).append("s");
            }
            if (sb.isEmpty()) {
                sb.append("0s");
            }
            return sb.toString();
        }
    }
}
