package org.leavesmc.leaves.entity.bot.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.ServerUseItemAutoOffhandAction;
import org.leavesmc.leaves.entity.bot.action.UseItemAutoOffhandAction;

import java.util.UUID;
import java.util.function.Consumer;

public class CraftUseItemAutoOffhandAction implements UseItemAutoOffhandAction {
    private final ServerUseItemAutoOffhandAction serverAction;
    private Consumer<UseItemAutoOffhandAction> onFail = null;
    private Consumer<UseItemAutoOffhandAction> onSuccess = null;
    private Consumer<UseItemAutoOffhandAction> onStop = null;

    public CraftUseItemAutoOffhandAction(ServerUseItemAutoOffhandAction serverAction) {
        this.serverAction = serverAction;
    }

    public boolean doTick(@NotNull ServerBot bot) {
        return serverAction.doTick(bot);
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
    public void setOnFail(Consumer<UseItemAutoOffhandAction> onFail) {
        this.onFail = onFail;
        serverAction.setOnFail(it -> onFail.accept(new CraftUseItemAutoOffhandAction(it)));
    }

    @Override
    public Consumer<UseItemAutoOffhandAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<UseItemAutoOffhandAction> onSuccess) {
        this.onSuccess = onSuccess;
        serverAction.setOnSuccess(it -> onSuccess.accept(new CraftUseItemAutoOffhandAction(it)));
    }

    @Override
    public Consumer<UseItemAutoOffhandAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<UseItemAutoOffhandAction> onStop) {
        this.onStop = onStop;
        serverAction.setOnStop(it -> onStop.accept(new CraftUseItemAutoOffhandAction(it)));
    }

    @Override
    public Consumer<UseItemAutoOffhandAction> getOnStop() {
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
