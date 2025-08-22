package org.leavesmc.leaves.neo_command.bot.subcommands.action;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.ServerBotAction;
import org.leavesmc.leaves.neo_command.CommandContext;
import org.leavesmc.leaves.neo_command.LiteralNode;
import org.leavesmc.leaves.neo_command.bot.subcommands.ActionCommand;

import java.util.List;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.spaces;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class ListCommand extends LiteralNode {

    public ListCommand() {
        super("list");
    }

    @Override
    protected boolean execute(@NotNull CommandContext context) {
        ServerBot bot = ActionCommand.BotArgument.getBot(context);
        if (bot == null) {
            return false;
        }

        List<ServerBotAction<?>> actions = bot.getBotActions();
        if (actions.isEmpty()) {
            context.getSender().sendMessage("This bot has no active actions");
            return true;
        }

        context.getSender().sendMessage(bot.getScoreboardName() + "'s action list:");
        for (int i = 0; i < actions.size(); i++) {
            ServerBotAction<?> action = actions.get(i);
            context.getSender().sendMessage(join(spaces(),
                text(i, GRAY),
                text(action.getName(), AQUA).hoverEvent(showText(text(action.getReadableActionDataString())))
            ));
        }

        return true;
    }
}
