package org.leavesmc.leaves.entity.bot.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.entity.bot.action.*;

import java.util.UUID;
import java.util.function.Consumer;

public class CraftUseItemToOffhandAction extends CraftBotAction implements UseItemToOffhandAction {
    private final ServerUseItemToOffhandAction serverAction;
    private Consumer<UseItemToOffhandAction> onFail = null;
    private Consumer<UseItemToOffhandAction> onSuccess = null;
    private Consumer<UseItemToOffhandAction> onStop = null;

    public CraftUseItemToOffhandAction(ServerUseItemToOffhandAction serverAction) {
        this.serverAction = serverAction;
    }

    public boolean doTick(@NotNull ServerBot bot) {
        return serverAction.doTick(bot);
    }

    @Override
    public int getUseTick() {
        return serverAction.getUseTick();
    }

    @Override
    public CraftUseItemToOffhandAction setUseTick(int useTick) {
        serverAction.setUseTick(useTick);
        return this;
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
    public void setOnFail(Consumer<UseItemToOffhandAction> onFail) {
        this.onFail = onFail;
        serverAction.setOnFail(it -> onFail.accept(new CraftUseItemToOffhandAction(it)));
    }

    @Override
    public Consumer<UseItemToOffhandAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<UseItemToOffhandAction> onSuccess) {
        this.onSuccess = onSuccess;
        serverAction.setOnSuccess(it -> onSuccess.accept(new CraftUseItemToOffhandAction(it)));
    }

    @Override
    public Consumer<UseItemToOffhandAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<UseItemToOffhandAction> onStop) {
        this.onStop = onStop;
        serverAction.setOnStop(it -> onStop.accept(new CraftUseItemToOffhandAction(it)));
    }

    @Override
    public Consumer<UseItemToOffhandAction> getOnStop() {
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
