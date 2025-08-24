package org.leavesmc.leaves.neo_command.bot;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.neo_command.CommandNode;
import org.leavesmc.leaves.neo_command.CommandUtils;
import org.leavesmc.leaves.neo_command.LiteralNode;
import org.leavesmc.leaves.neo_command.bot.subcommands.ActionCommand;
import org.leavesmc.leaves.neo_command.bot.subcommands.ConfigCommand;
import org.leavesmc.leaves.neo_command.bot.subcommands.CreateCommand;
import org.leavesmc.leaves.neo_command.bot.subcommands.ListCommand;
import org.leavesmc.leaves.neo_command.bot.subcommands.LoadCommand;
import org.leavesmc.leaves.neo_command.bot.subcommands.SaveCommand;

import java.util.ArrayList;
import java.util.List;

public class BotCommand extends LiteralNode {
    public static final BotCommand INSTANCE = new BotCommand();
    private static final String PERM_BASE = "bukkit.command.bot";

    private BotCommand() {
        super("bot");
        this.children(
            ActionCommand::new,
            ListCommand::new,
            CreateCommand::new,
            LoadCommand::new,
            SaveCommand::new,
            ConfigCommand::new
        );
    }

    @Override
    protected ArgumentBuilder<CommandSourceStack, ?> compile() {
        List<String> permissions = new ArrayList<>();
        permissions.add(PERM_BASE);
        permissions.addAll(this.children.stream().map(CommandNode::getName).toList());
        CommandUtils.registerPermissions(permissions);
        return super.compile();
    }

    @Override
    public boolean requires(@NotNull CommandSourceStack source) {
        return children.stream().anyMatch(child -> child.requires(source));
    }

    public static boolean hasPermission(@NotNull CommandSender sender) {
        return sender.hasPermission(PERM_BASE);
    }

    public static boolean hasPermission(@NotNull CommandSender sender, String subcommand) {
        return sender.hasPermission(PERM_BASE) || sender.hasPermission(PERM_BASE + "." + subcommand);
    }
}
