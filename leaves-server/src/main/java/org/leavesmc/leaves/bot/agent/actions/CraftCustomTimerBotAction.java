package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.AbstractCustomTimerBotAction;

public class CraftCustomTimerBotAction extends CraftTimerBotAction<AbstractCustomTimerBotAction> implements CraftCustomAction<CraftCustomTimerBotAction> {

    private final AbstractCustomTimerBotAction realAction;

    public CraftCustomTimerBotAction(String name, @NotNull AbstractCustomTimerBotAction realAction) {
        super(name, null);
        this.realAction = realAction;
    }

    @Override
    public CraftCustomTimerBotAction createCraft(@Nullable Player player, String[] args) {
        AbstractCustomTimerBotAction newRealAction = realAction.getNew(player, args);
        if (newRealAction != null) {
            return new CraftCustomTimerBotAction(this.getName(), newRealAction);
        }
        return null;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        return realAction.doTick(bot.getBukkitEntity());
    }

    @Override
    public @NotNull Class<? extends AbstractCustomTimerBotAction> getActionRegClass() {
        return realAction.getClass();
    }
}
