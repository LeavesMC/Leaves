package org.leavesmc.leaves.command.leaves.subcommands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.leaves.LeavesSubcommand;
import org.leavesmc.leaves.util.LeavesUpdateHelper;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class UpdateCommand extends LeavesSubcommand {

    public UpdateCommand() {
        super("update");
    }

    @Override
    protected boolean execute(@NotNull CommandContext context) throws CommandSyntaxException {
        CommandSender sender = context.getSender();
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(text("Trying to update Leaves...", GRAY));
        } else {
            sender.sendMessage(text("Trying to update Leaves, see the console for more info", GRAY));
        }
        LeavesUpdateHelper.tryUpdateLeaves();
        return true;
    }
}
