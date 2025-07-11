package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.CustomBotAction;
import org.leavesmc.leaves.entity.bot.actions.CraftCustomTimerBotAction;

public class ServerCustomTimerBotAction extends ServerTimerBotAction<ServerCustomTimerBotAction> {
    private final CustomBotAction realAction;

    public ServerCustomTimerBotAction(String name, @NotNull CustomBotAction realAction) {
        super(name, null);
        this.realAction = realAction;
    }

    public void loadRealActionCommand(@NotNull Player player, @NotNull String @NotNull [] args) {
        realAction.loadCommand(player, args);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        return realAction.doTick(bot.getBukkitEntity());
    }

    @Override
    public Class<?> getActionClass() {
        return realAction.getClass();
    }

    @Override
    public Object asCraft() {
        return new CraftCustomTimerBotAction(this);
    }

    public CustomBotAction getRealAction() {
        return realAction;
    }
}
