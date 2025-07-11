package org.leavesmc.leaves.entity.bot.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.ServerCustomBotAction;
import org.leavesmc.leaves.bot.agent.actions.ServerCustomStateBotAction;
import org.leavesmc.leaves.bot.agent.actions.ServerStateBotAction;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.Bot;
import org.leavesmc.leaves.entity.bot.CraftBot;
import org.leavesmc.leaves.entity.bot.action.AbstractCustomBotAction;
import org.leavesmc.leaves.entity.bot.action.AbstractCustomStateBotAction;
import org.leavesmc.leaves.entity.bot.action.CustomBotAction;

import java.util.List;

public class CraftCustomStateBotAction implements CustomBotAction {

    private final ServerCustomStateBotAction serverAction;
    private final CustomBotAction realAction;

    @SuppressWarnings("unchecked")
    public <T extends AbstractCustomStateBotAction<T>> CraftCustomStateBotAction(AbstractCustomStateBotAction<T> apiAction) {
        this.realAction = apiAction;
        this.serverAction = new ServerCustomStateBotAction(apiAction.getName(), apiAction);
        this.serverAction.setCancelled(apiAction.isCancelled());
        this.serverAction.setOnSuccess(it -> apiAction.getOnSuccess().accept((T) apiAction));
        this.serverAction.setOnFail(it -> apiAction.getOnFail().accept((T) apiAction));
        this.serverAction.setOnStop(it -> apiAction.getOnStop().accept((T) apiAction));
    }

    public CraftCustomStateBotAction(@NotNull ServerCustomStateBotAction serverAction) {
        this.serverAction = serverAction;
        this.realAction = serverAction.getRealAction();
    }

    @Override
    public boolean doTick(Bot bot) {
        return serverAction.doTick(((CraftBot) bot).getHandle());
    }

    @Override
    public void loadCommand(@Nullable Player player, String[] args) {
        realAction.loadCommand(player, args);
    }

    @Override
    public @NotNull List<List<String>> getTabComplete() {
        return realAction.getTabComplete();
    }

    public ServerCustomStateBotAction getHandle() {
        return serverAction;
    }
}
