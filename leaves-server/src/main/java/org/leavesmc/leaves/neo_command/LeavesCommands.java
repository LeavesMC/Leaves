package org.leavesmc.leaves.neo_command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class LeavesCommands {
    public static void registerLeavesCommands(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        new LeavesCommand().register(dispatcher);
    }
}
