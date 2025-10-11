package org.leavesmc.leaves.command.bot.subcommands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.ArgumentNode;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.arguments.BotArgumentType;
import org.leavesmc.leaves.command.bot.BotSubcommand;
import org.leavesmc.leaves.command.bot.subcommands.action.ListCommand;
import org.leavesmc.leaves.command.bot.subcommands.action.StartCommand;
import org.leavesmc.leaves.command.bot.subcommands.action.StopCommand;

public class ActionCommand extends BotSubcommand {

    public ActionCommand() {
        super("action");
        children(BotArgument::new);
    }

    @Override
    public boolean requires(@NotNull CommandSourceStack source) {
        return LeavesConfig.modify.fakeplayer.canUseAction && super.requires(source);
    }

    public static class BotArgument extends ArgumentNode<ServerBot> {

        private BotArgument() {
            super("bot", BotArgumentType.bot());
            children(
                StartCommand::new,
                StopCommand::new,
                ListCommand::new
            );
        }

        public static @NotNull ServerBot getBot(@NotNull CommandContext context) {
            return context.getArgument(BotArgument.class);
        }
    }
}