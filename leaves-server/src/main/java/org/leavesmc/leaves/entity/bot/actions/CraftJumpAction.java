package org.leavesmc.leaves.entity.bot.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.entity.bot.action.*;

import java.util.UUID;
import java.util.function.Consumer;

public class CraftJumpAction extends CraftBotAction implements JumpAction {
    private final ServerJumpAction serverAction;
    private Consumer<JumpAction> onFail = null;
    private Consumer<JumpAction> onSuccess = null;
    private Consumer<JumpAction> onStop = null;

    public CraftJumpAction(ServerJumpAction serverAction) {
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
    public void setOnFail(Consumer<JumpAction> onFail) {
        this.onFail = onFail;
        serverAction.setOnFail(it -> onFail.accept(new CraftJumpAction(it)));
    }

    @Override
    public Consumer<JumpAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<JumpAction> onSuccess) {
        this.onSuccess = onSuccess;
        serverAction.setOnSuccess(it -> onSuccess.accept(new CraftJumpAction(it)));
    }

    @Override
    public Consumer<JumpAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<JumpAction> onStop) {
        this.onStop = onStop;
        serverAction.setOnStop(it -> onStop.accept(new CraftJumpAction(it)));
    }

    @Override
    public Consumer<JumpAction> getOnStop() {
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
