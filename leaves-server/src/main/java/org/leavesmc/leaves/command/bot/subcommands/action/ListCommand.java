package org.leavesmc.leaves.command.bot.subcommands.action;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.AbstractBotAction;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.LiteralNode;
import org.leavesmc.leaves.command.bot.subcommands.ActionCommand;

import java.util.List;

import static io.papermc.paper.adventure.PaperAdventure.asAdventure;
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
    protected boolean execute(@NotNull CommandContext context) throws CommandSyntaxException {
        ServerBot bot = ActionCommand.BotArgument.getBot(context);

        CommandSender sender = context.getSender();
        List<AbstractBotAction<?>> actions = bot.getBotActions();
        if (actions.isEmpty()) {
            sender.sendMessage(text("This bot has no active actions", GRAY));
            return true;
        }

        sender.sendMessage(asAdventure(bot.getDisplayName()).append(text("'s action list:", GRAY)));
        for (int i = 0; i < actions.size(); i++) {
            AbstractBotAction<?> action = actions.get(i);
            sender.sendMessage(join(spaces(),
                text(i, GRAY),
                text(action.getName(), AQUA).hoverEvent(showText(text(action.getActionDataString())))
            ));
        }

        return true;
    }
}
