package org.leavesmc.leaves.neo_command.bot;

import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.neo_command.LiteralNode;
import org.leavesmc.leaves.neo_command.bot.subcommands.ActionCommand;

public class BotCommand extends LiteralNode {
    private static final String PERM_BASE = "bukkit.command.bot";

    public BotCommand() {
        super("bot_neo");
        this.children(
            ActionCommand::new
        );
    }

    @Override
    protected boolean requires(CommandSourceStack source) {
        return LeavesConfig.modify.fakeplayer.enable && source.getSender().hasPermission(PERM_BASE);
    }

    public static boolean hasPermission(@NotNull CommandSourceStack source, String subcommand) {
        CommandSender sender = source.getSender();
        return sender.hasPermission(PERM_BASE) || sender.hasPermission(PERM_BASE + "." + subcommand);
    }
}
