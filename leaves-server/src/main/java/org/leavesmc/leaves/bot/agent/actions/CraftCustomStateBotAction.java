package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.action.CustomStateBotAction;

public class CraftCustomStateBotAction extends CraftStateBotAction<CustomStateBotAction> implements CraftCustomAction<CraftCustomStateBotAction> {

    private final CustomStateBotAction realAction;

    public CraftCustomStateBotAction(String name, @NotNull CustomStateBotAction realAction) {
        super(name, CommandArgument.EMPTY, null);
        this.realAction = realAction;
    }

    @Override
    public CraftCustomStateBotAction createCraft(@Nullable Player player, String[] args) {
        CustomStateBotAction newRealAction = realAction.getNew(player, args);
        if (newRealAction != null) {
            return new CraftCustomStateBotAction(this.getName(), newRealAction);
        }
        return null;
    }

    @Override
    public Class<CustomStateBotAction> getInterfaceClass() {
        return null;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        return realAction.doTick(bot.getBukkitEntity());
    }
}
