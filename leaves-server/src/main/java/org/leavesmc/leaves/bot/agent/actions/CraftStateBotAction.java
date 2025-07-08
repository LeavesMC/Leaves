package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.entity.bot.action.StateBotAction;

import java.util.function.Supplier;

public abstract class CraftStateBotAction<E extends StateBotAction<E>> extends CraftBotAction<E> implements StateBotAction<E> {
    public CraftStateBotAction(String name, CommandArgument argument, Supplier<E> creator) {
        super(name, argument, creator);
        this.setDoNumber0(-1);
    }
}
