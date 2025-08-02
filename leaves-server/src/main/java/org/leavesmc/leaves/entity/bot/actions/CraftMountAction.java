package org.leavesmc.leaves.entity.bot.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.entity.bot.action.*;

import java.util.UUID;
import java.util.function.Consumer;

public class CraftMountAction extends CraftBotAction implements MountAction {
    private final ServerMountAction serverAction;
    private Consumer<MountAction> onFail = null;
    private Consumer<MountAction> onSuccess = null;
    private Consumer<MountAction> onStop = null;

    public CraftMountAction(ServerMountAction serverAction) {
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
    public void setOnFail(Consumer<MountAction> onFail) {
        this.onFail = onFail;
        serverAction.setOnFail(it -> onFail.accept(new CraftMountAction(it)));
    }

    @Override
    public Consumer<MountAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<MountAction> onSuccess) {
        this.onSuccess = onSuccess;
        serverAction.setOnSuccess(it -> onSuccess.accept(new CraftMountAction(it)));
    }

    @Override
    public Consumer<MountAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<MountAction> onStop) {
        this.onStop = onStop;
        serverAction.setOnStop(it -> onStop.accept(new CraftMountAction(it)));
    }

    @Override
    public Consumer<MountAction> getOnStop() {
        return onStop;
    }
}
