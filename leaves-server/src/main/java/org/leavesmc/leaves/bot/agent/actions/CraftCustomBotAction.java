package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.action.AbstractCustomBotAction;

public class CraftCustomBotAction extends CraftBotAction<AbstractCustomBotAction> implements CraftCustomAction<CraftCustomBotAction> {

    private final AbstractCustomBotAction realAction;

    public CraftCustomBotAction(String name, @NotNull AbstractCustomBotAction realAction) {
        super(name, CommandArgument.EMPTY, null);
        this.realAction = realAction;
    }

    @Override
    public CraftCustomBotAction createCraft(@Nullable Player player, String[] args) {
        AbstractCustomBotAction newRealAction = realAction.getNew(player, args);
        if (newRealAction != null) {
            return new CraftCustomBotAction(this.getName(), newRealAction);
        }
        return null;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        return realAction.doTick(bot.getBukkitEntity());
    }

    @Override
    public Class<AbstractCustomBotAction> getInterfaceClass() {
        return null;
    }

    @Override
    public Class<?> getRealActionClass() {
        return realAction.getClass();
    }
}
