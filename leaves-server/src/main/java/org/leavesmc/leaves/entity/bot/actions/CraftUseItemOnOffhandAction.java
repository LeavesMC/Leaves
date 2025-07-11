package org.leavesmc.leaves.entity.bot.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.ServerUseItemOnOffhandAction;
import org.leavesmc.leaves.entity.bot.action.UseItemOnOffhandAction;

import java.util.UUID;
import java.util.function.Consumer;

public class CraftUseItemOnOffhandAction implements UseItemOnOffhandAction {
    private final ServerUseItemOnOffhandAction serverAction;
    private Consumer<UseItemOnOffhandAction> onFail = null;
    private Consumer<UseItemOnOffhandAction> onSuccess = null;
    private Consumer<UseItemOnOffhandAction> onStop = null;

    public CraftUseItemOnOffhandAction(ServerUseItemOnOffhandAction serverAction) {
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
    public void setOnFail(Consumer<UseItemOnOffhandAction> onFail) {
        this.onFail = onFail;
        serverAction.setOnFail(it -> onFail.accept(new CraftUseItemOnOffhandAction(it)));
    }

    @Override
    public Consumer<UseItemOnOffhandAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<UseItemOnOffhandAction> onSuccess) {
        this.onSuccess = onSuccess;
        serverAction.setOnSuccess(it -> onSuccess.accept(new CraftUseItemOnOffhandAction(it)));
    }

    @Override
    public Consumer<UseItemOnOffhandAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<UseItemOnOffhandAction> onStop) {
        this.onStop = onStop;
        serverAction.setOnStop(it -> onStop.accept(new CraftUseItemOnOffhandAction(it)));
    }

    @Override
    public Consumer<UseItemOnOffhandAction> getOnStop() {
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
