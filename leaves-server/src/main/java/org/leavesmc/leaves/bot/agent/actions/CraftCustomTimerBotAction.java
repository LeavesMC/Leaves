package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.action.CustomTimerBotAction;

public class CraftCustomTimerBotAction extends CraftTimerBotAction<CustomTimerBotAction> implements CraftCustomAction<CraftCustomTimerBotAction> {

    private final CustomTimerBotAction realAction;

    public CraftCustomTimerBotAction(String name, @NotNull CustomTimerBotAction realAction) {
        super(name, CommandArgument.EMPTY, null);
        this.realAction = realAction;
    }

    @Override
    public Class<CustomTimerBotAction> getInterfaceClass() {
        return null;
    }

    @Override
    public CraftCustomTimerBotAction createCraft(@Nullable Player player, String[] args) {
        CustomTimerBotAction newRealAction = realAction.getNew(player, args);
        if (newRealAction != null) {
            return new CraftCustomTimerBotAction(this.getName(), newRealAction);
        }
        return null;
    }

    @Override
    public int getDoNumber() {
        return realAction.getDoNumber();
    }

    @Override
    public int getStartDelayTick() {
        return realAction.getStartDelayTick();
    }

    @Override
    public int getDoIntervalTick() {
        return realAction.getDoIntervalTick();
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        return realAction.doTick(bot.getBukkitEntity());
    }
}
