package org.leavesmc.leaves.entity.bot.action.custom;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface BrigadierLikeProcessor extends CommandProcessor {

    void buildCommand(LiteralArgumentBuilder<CommandSourceStack> root);

}
