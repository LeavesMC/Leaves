package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.AbstractBotAction;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.entity.botaction.CustomBotAction;

public class CraftCustomBotAction extends AbstractBotAction<CraftCustomBotAction> {

    private final CustomBotAction realAction;

    public CraftCustomBotAction(String name, @NotNull CustomBotAction realAction) {
        super(name, CommandArgument.EMPTY, null);
        this.realAction = realAction;
    }

    @Override
    public void loadCommand(@Nullable ServerPlayer player, @NotNull CommandArgumentResult result) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public CraftCustomBotAction createCraft(@Nullable Player player, String[] args) {
        CustomBotAction newRealAction = realAction.getNew(player, args);
        if (newRealAction != null) {
            return new CraftCustomBotAction(this.getName(), newRealAction);
        }
        return null;
    }

    @Override
    public int getInitialNumber() {
        return realAction.getInitialNumber();
    }

    @Override
    public int getInitialTickDelay() {
        return realAction.getInitialTickDelay();
    }

    @Override
    public int getInitialTickInterval() {
        return realAction.getInitialTickInterval();
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        return realAction.doTick(bot.getBukkitEntity());
    }
}
