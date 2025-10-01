package org.leavesmc.leaves.command.bot;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.command.RootNode;
import org.leavesmc.leaves.command.bot.subcommands.ActionCommand;
import org.leavesmc.leaves.command.bot.subcommands.ConfigCommand;
import org.leavesmc.leaves.command.bot.subcommands.CreateCommand;
import org.leavesmc.leaves.command.bot.subcommands.ListCommand;
import org.leavesmc.leaves.command.bot.subcommands.LoadCommand;
import org.leavesmc.leaves.command.bot.subcommands.RemoveCommand;
import org.leavesmc.leaves.command.bot.subcommands.SaveCommand;

import static org.leavesmc.leaves.command.CommandUtils.registerPermissions;

public class BotCommand extends RootNode {
    public static final BotCommand INSTANCE = new BotCommand();
    private static final String PERM_BASE = "bukkit.command.bot";

    private BotCommand() {
        super("bot", PERM_BASE);
        this.children(
            ListCommand::new,
            ConfigCommand::new,
            RemoveCommand::new,
            LoadCommand::new,
            SaveCommand::new,
            ActionCommand::new,
            CreateCommand::new
        );
    }

    @Override
    protected ArgumentBuilder<CommandSourceStack, ?> compile() {
        registerPermissions(PERM_BASE, this.children);
        return super.compile();
    }

    public static boolean hasPermission(@NotNull CommandSender sender) {
        return sender.hasPermission(PERM_BASE);
    }

    public static boolean hasPermission(@NotNull CommandSender sender, String subcommand) {
        return sender.hasPermission(PERM_BASE) || sender.hasPermission(PERM_BASE + "." + subcommand);
    }
}
