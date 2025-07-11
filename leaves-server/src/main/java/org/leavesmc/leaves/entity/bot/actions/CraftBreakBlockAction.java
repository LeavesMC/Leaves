package org.leavesmc.leaves.entity.bot.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.ServerBreakBlockAction;
import org.leavesmc.leaves.entity.bot.action.BreakBlockAction;

import java.util.UUID;
import java.util.function.Consumer;

public class CraftBreakBlockAction implements BreakBlockAction {
    private final ServerBreakBlockAction serverAction;
    private Consumer<BreakBlockAction> onFail = null;
    private Consumer<BreakBlockAction> onSuccess = null;
    private Consumer<BreakBlockAction> onStop = null;

    public CraftBreakBlockAction(ServerBreakBlockAction serverAction) {
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
    public void setOnFail(Consumer<BreakBlockAction> onFail) {
        this.onFail = onFail;
        serverAction.setOnFail(it -> onFail.accept(new CraftBreakBlockAction(it)));
    }

    @Override
    public Consumer<BreakBlockAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<BreakBlockAction> onSuccess) {
        this.onSuccess = onSuccess;
        serverAction.setOnSuccess(it -> onSuccess.accept(new CraftBreakBlockAction(it)));
    }

    @Override
    public Consumer<BreakBlockAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<BreakBlockAction> onStop) {
        this.onStop = onStop;
        serverAction.setOnStop(it -> onStop.accept(new CraftBreakBlockAction(it)));
    }

    @Override
    public Consumer<BreakBlockAction> getOnStop() {
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
