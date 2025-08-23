package org.leavesmc.leaves.neo_command.leaves;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.neo_command.CommandNode;
import org.leavesmc.leaves.neo_command.CommandUtils;
import org.leavesmc.leaves.neo_command.LiteralNode;
import org.leavesmc.leaves.neo_command.leaves.subcommands.ConfigCommand;

import java.util.ArrayList;
import java.util.List;

public class LeavesCommand extends LiteralNode {
    public static final LeavesCommand INSTANCE = new LeavesCommand();
    private static final String PERM_BASE = "bukkit.command.leaves";

    private LeavesCommand() {
        super("leaves_new");
        children(ConfigCommand::new);
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

    public static boolean hasPermission(@NotNull CommandSender sender, String subcommand) {
        return sender.hasPermission(PERM_BASE) || sender.hasPermission(PERM_BASE + "." + subcommand);
    }
}
