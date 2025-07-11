package org.leavesmc.leaves.entity.bot.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.ServerBotAction;
import org.leavesmc.leaves.bot.agent.actions.ServerUseItemToAction;
import org.leavesmc.leaves.entity.bot.action.UseItemToAction;

import java.util.UUID;
import java.util.function.Consumer;

public class CraftUseItemToAction extends CraftBotAction implements UseItemToAction {
    private final ServerUseItemToAction serverAction;
    private Consumer<UseItemToAction> onFail = null;
    private Consumer<UseItemToAction> onSuccess = null;
    private Consumer<UseItemToAction> onStop = null;

    public CraftUseItemToAction(ServerUseItemToAction serverAction) {
        this.serverAction = serverAction;
    }

    public boolean doTick(@NotNull ServerBot bot) {
        return serverAction.doTick(bot);
    }

    @Override
    public ServerBotAction<?> getHandle() {
        return serverAction;
    }

    @Override
    public String getName() {
        return serverAction.getName();
    }

    @Override
    public UUID getUUID() {
        return serverAction.getUUID();
    }

    @Override
    public void setCancelled(boolean cancel) {
        serverAction.setCancelled(cancel);
    }

    @Override
    public boolean isCancelled() {
        return serverAction.isCancelled();
    }

    @Override
    public void setOnFail(Consumer<UseItemToAction> onFail) {
        this.onFail = onFail;
        serverAction.setOnFail(it -> onFail.accept(new CraftUseItemToAction(it)));
    }

    @Override
    public Consumer<UseItemToAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<UseItemToAction> onSuccess) {
        this.onSuccess = onSuccess;
        serverAction.setOnSuccess(it -> onSuccess.accept(new CraftUseItemToAction(it)));
    }

    @Override
    public Consumer<UseItemToAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<UseItemToAction> onStop) {
        this.onStop = onStop;
        serverAction.setOnStop(it -> onStop.accept(new CraftUseItemToAction(it)));
    }

    @Override
    public Consumer<UseItemToAction> getOnStop() {
        return onStop;
    }

    @Override
    public void setStartDelayTick(int delayTick) {
        serverAction.setStartDelayTick(delayTick);
    }

    @Override
    public int getStartDelayTick() {
        return serverAction.getStartDelayTick();
    }

    @Override
    public void setDoIntervalTick(int intervalTick) {
        serverAction.setDoIntervalTick(intervalTick);
    }

    @Override
    public int getDoIntervalTick() {
        return serverAction.getDoIntervalTick();
    }

    @Override
    public void setDoNumber(int doNumber) {
        serverAction.setDoNumber(doNumber);
    }

    @Override
    public int getDoNumber() {
        return serverAction.getDoNumber();
    }

    @Override
    public int getTickToNext() {
        return serverAction.getTickToNext();
    }

    @Override
    public int getDoNumberRemaining() {
        return serverAction.getDoNumberRemaining();
    }
}
