package org.leavesmc.leaves.neo_command.bot.subcommands.action;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.ServerBotAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;
import org.leavesmc.leaves.neo_command.ArgumentNode;
import org.leavesmc.leaves.neo_command.CommandContext;
import org.leavesmc.leaves.neo_command.LiteralNode;
import org.leavesmc.leaves.neo_command.bot.subcommands.ActionCommand;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static io.papermc.paper.adventure.PaperAdventure.asAdventure;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.spaces;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class StopCommand extends LiteralNode {

    public StopCommand() {
        super("stop");
        children(StopIndexArgument::new);
        children(StopAll::new);
    }

    private static class StopIndexArgument extends ArgumentNode<Integer> {

        protected StopIndexArgument() {
            super("index", IntegerArgumentType.integer(0));
        }

        @Override
        protected CompletableFuture<Suggestions> getSuggestions(CommandContext context, SuggestionsBuilder builder) {
            ServerBot bot = ActionCommand.BotArgument.getBot(context);
            if (bot == null) {
                return Suggestions.empty();
            }
            for (int i = 0; i < bot.getBotActions().size(); i++) {
                ServerBotAction<?> action = bot.getBotActions().get(i);
                builder.suggest(String.valueOf(i), Component.literal(action.getName()));
            }
            return builder.buildFuture();
        }

        @Override
        protected boolean execute(CommandContext context) {
            ServerBot bot = ActionCommand.BotArgument.getBot(context);
            CommandSender sender = context.getSender();
            if (bot == null) {
                return false;
            }
            int index = context.getArgument("index", Integer.class);
            if (index < 0 || index >= bot.getBotActions().size()) {
                sender.sendMessage(text("Invalid index.", RED));
                return false;
            }

            ServerBotAction<?> action = bot.getBotActions().get(index);
            BotActionStopEvent event = new BotActionStopEvent(
                bot.getBukkitEntity(), action.getName(), action.getUUID(), BotActionStopEvent.Reason.COMMAND, sender
            );
            event.callEvent();
            if (!event.isCancelled()) {
                action.stop(bot, BotActionStopEvent.Reason.COMMAND);
                bot.getBotActions().remove(index);
                sender.sendMessage(join(spaces(),
                    text("Already stopped", GRAY),
                    asAdventure(bot.getDisplayName()).append(text("'s", GRAY)),
                    text("action", GRAY),
                    text(action.getName(), AQUA).hoverEvent(showText(text(action.getReadableActionDataString())))
                ));
            } else {
                sender.sendMessage(text("Action stop cancelled by a plugin", RED));
            }
            return true;
        }
    }

    private static class StopAll extends LiteralNode {

        public StopAll() {
            super("all");
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) {
            ServerBot bot = ActionCommand.BotArgument.getBot(context);
            if (bot == null) {
                return false;
            }
            Set<ServerBotAction<?>> canceled = new HashSet<>();
            Set<ServerBotAction<?>> forRemoval = new HashSet<>();
            for (int i = 0; i < bot.getBotActions().size(); i++) {
                ServerBotAction<?> action = bot.getBotActions().get(i);
                BotActionStopEvent event = new BotActionStopEvent(
                    bot.getBukkitEntity(), action.getName(), action.getUUID(), BotActionStopEvent.Reason.COMMAND, context.getSender()
                );
                event.callEvent();
                if (!event.isCancelled()) {
                    forRemoval.add(action);
                    action.stop(bot, BotActionStopEvent.Reason.COMMAND);
                } else {
                    canceled.add(action);
                }
            }
            bot.getBotActions().removeAll(forRemoval);
            if (canceled.isEmpty()) {
                context.getSender().sendMessage(bot.getScoreboardName() + "'s action list cleared.");
            } else {
                context.getSender().sendMessage("already tried to clear" + bot.getScoreboardName() + "'s action list, but following actions' stop was canceled by plugin:");
                for (ServerBotAction<?> action : canceled) {
                    context.getSender().sendMessage(
                        text(action.getName(), AQUA)
                            .hoverEvent(showText(text(action.getReadableActionDataString())))
                    );
                }
            }
            return true;
        }
    }
}
