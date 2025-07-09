package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.action.AbstractCustomStateBotAction;

public class CraftCustomStateBotAction extends CraftStateBotAction<AbstractCustomStateBotAction> implements CraftCustomAction<CraftCustomStateBotAction> {

    private final AbstractCustomStateBotAction realAction;

    public CraftCustomStateBotAction(String name, @NotNull AbstractCustomStateBotAction realAction) {
        super(name, CommandArgument.EMPTY, null);
        this.realAction = realAction;
    }

    @Override
    public CraftCustomStateBotAction createCraft(@Nullable Player player, String[] args) {
        AbstractCustomStateBotAction newRealAction = realAction.getNew(player, args);
        if (newRealAction != null) {
            return new CraftCustomStateBotAction(this.getName(), newRealAction);
        }
        return null;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        return realAction.doTick(bot.getBukkitEntity());
    }

    @Override
    public @NotNull Class<? extends AbstractCustomStateBotAction> getActionRegClass() {
        return realAction.getClass();
    }
}
