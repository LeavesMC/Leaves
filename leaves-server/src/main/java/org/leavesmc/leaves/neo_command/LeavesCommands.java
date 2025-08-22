package org.leavesmc.leaves.neo_command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.leavesmc.leaves.neo_command.bot.BotCommand;
import org.leavesmc.leaves.neo_command.leaves.LeavesCommand;

public class LeavesCommands {
    public static void registerLeavesCommands() {
        CommandDispatcher<CommandSourceStack> dispatcher = MinecraftServer.getServer().getCommands().getDispatcher();
        new LeavesCommand().register(dispatcher);
        new BotCommand().register(dispatcher);
    }
}
