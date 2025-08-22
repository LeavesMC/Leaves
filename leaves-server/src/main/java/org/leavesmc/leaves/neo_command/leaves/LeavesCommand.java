package org.leavesmc.leaves.neo_command.leaves;

import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.neo_command.LiteralNode;
import org.leavesmc.leaves.neo_command.leaves.subcommands.ConfigCommand;

public class LeavesCommand extends LiteralNode {
    private static final String PERM_BASE = "bukkit.command.leaves";

    public LeavesCommand() {
        super("leaves_new");
        children(ConfigCommand::new);
    }

    @Override
    protected boolean requires(@NotNull CommandSourceStack source) {
        return source.getSender().hasPermission(PERM_BASE);
    }

    public static boolean hasPermission(@NotNull CommandSourceStack source, String subcommand) {
        CommandSender sender = source.getSender();
        return sender.hasPermission(PERM_BASE) || sender.hasPermission(PERM_BASE + "." + subcommand);
    }
}
