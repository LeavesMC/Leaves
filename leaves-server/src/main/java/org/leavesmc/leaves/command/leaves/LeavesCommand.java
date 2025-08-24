package org.leavesmc.leaves.command.leaves;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.command.LiteralNode;
import org.leavesmc.leaves.command.leaves.subcommands.BlockUpdateCommand;
import org.leavesmc.leaves.command.leaves.subcommands.ConfigCommand;
import org.leavesmc.leaves.command.leaves.subcommands.CounterCommand;
import org.leavesmc.leaves.command.leaves.subcommands.ReloadCommand;
import org.leavesmc.leaves.command.leaves.subcommands.ReportCommand;
import org.leavesmc.leaves.command.leaves.subcommands.UpdateCommand;

import static org.leavesmc.leaves.command.CommandUtils.registerPermissions;

public class LeavesCommand extends LiteralNode {
    public static final LeavesCommand INSTANCE = new LeavesCommand();
    private static final String PERM_BASE = "bukkit.command.leaves";

    private LeavesCommand() {
        super("leaves");
        children(
            BlockUpdateCommand::new,
            ConfigCommand::new,
            CounterCommand::new,
            ReloadCommand::new,
            ReportCommand::new,
            UpdateCommand::new
        );
    }

    @Override
    protected ArgumentBuilder<CommandSourceStack, ?> compile() {
        registerPermissions(PERM_BASE, this.children);
        return super.compile();
    }

    @Override
    public boolean requires(@NotNull CommandSourceStack source) {
        return children.stream().anyMatch(child -> child.requires(source));
    }

    public static boolean hasPermission(@NotNull CommandSender sender, String subcommand) {
        return sender.hasPermission(PERM_BASE) || sender.hasPermission(PERM_BASE + "." + subcommand);
    }
}
