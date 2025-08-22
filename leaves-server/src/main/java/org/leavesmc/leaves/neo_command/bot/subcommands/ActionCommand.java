package org.leavesmc.leaves.neo_command.bot.subcommands;

import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.neo_command.CommandContext;
import org.leavesmc.leaves.neo_command.CustomArgumentNode;
import org.leavesmc.leaves.neo_command.LiteralNode;
import org.leavesmc.leaves.neo_command.bot.BotCommand;
import org.leavesmc.leaves.neo_command.bot.subcommands.action.ListCommand;
import org.leavesmc.leaves.neo_command.bot.subcommands.action.StartCommand;
import org.leavesmc.leaves.neo_command.bot.subcommands.action.StopCommand;

public class ActionCommand extends LiteralNode {

    public ActionCommand() {
        super("action");
        children(ActionCommand.BotArgument::new);
    }

    @Override
    protected boolean requires(CommandSourceStack source) {
        return BotCommand.hasPermission(source, "action");
    }

    public static class BotArgument extends CustomArgumentNode<ServerBot, String> {

        protected BotArgument() {
            super("bot", new org.leavesmc.leaves.neo_command.bot.BotArgument());
            children(
                StartCommand::new,
                StopCommand::new,
                ListCommand::new
            );
        }

        public static @Nullable ServerBot getBot(@NotNull CommandContext context) {
            try {
                return context.getCustomArgument(BotArgument.class);
            } catch (IllegalArgumentException e) {
                context.getSender().sendMessage("This bot does not exist.");
                return null;
            }
        }
    }
}