package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.action.CustomBotAction;
import org.leavesmc.leaves.entity.bot.actions.CraftCustomStateBotAction;

public class ServerCustomStateBotAction extends ServerStateBotAction<ServerCustomStateBotAction> {
    private final CustomBotAction realAction;

    public ServerCustomStateBotAction(String name, @NotNull CustomBotAction realAction) {
        super(name, CommandArgument.EMPTY, null);
        this.realAction = realAction;
    }

    public void loadRealActionCommand(@NotNull Player player, @NotNull String[] args) {
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
        return new CraftCustomStateBotAction(this);
    }

    public CustomBotAction getRealAction() {
        return realAction;
    }
}
