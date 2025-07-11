package org.leavesmc.leaves.entity.bot.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.agent.actions.ServerCustomTimerBotAction;
import org.leavesmc.leaves.entity.bot.Bot;
import org.leavesmc.leaves.entity.bot.CraftBot;
import org.leavesmc.leaves.entity.bot.action.AbstractCustomTimerBotAction;
import org.leavesmc.leaves.entity.bot.action.CustomBotAction;

import java.util.List;

public class CraftCustomTimerBotAction extends CraftBotAction implements CustomBotAction {

    private final ServerCustomTimerBotAction serverAction;
    private final CustomBotAction realAction;

    @SuppressWarnings("unchecked")
    public <T extends AbstractCustomTimerBotAction<T>> CraftCustomTimerBotAction(AbstractCustomTimerBotAction<T> apiAction) {
        this.serverAction = new ServerCustomTimerBotAction(apiAction.getName(), apiAction);
        this.realAction = apiAction;
        this.serverAction.setCancelled(apiAction.isCancelled());
        this.serverAction.setOnSuccess(it -> apiAction.getOnSuccess().accept((T) apiAction));
        this.serverAction.setOnFail(it -> apiAction.getOnFail().accept((T) apiAction));
        this.serverAction.setOnStop(it -> apiAction.getOnStop().accept((T) apiAction));
        this.serverAction.setDoNumber(apiAction.getDoNumber());
        this.serverAction.setDoIntervalTick(apiAction.getDoIntervalTick());
        this.serverAction.setStartDelayTick(apiAction.getStartDelayTick());
    }

    public CraftCustomTimerBotAction(@NotNull ServerCustomTimerBotAction serverAction) {
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

    public ServerCustomTimerBotAction getHandle() {
        return serverAction;
    }
}
