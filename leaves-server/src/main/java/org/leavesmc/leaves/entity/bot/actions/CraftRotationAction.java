package org.leavesmc.leaves.entity.bot.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.ServerBotAction;
import org.leavesmc.leaves.bot.agent.actions.ServerRotationAction;
import org.leavesmc.leaves.entity.bot.action.RotationAction;

import java.util.UUID;
import java.util.function.Consumer;

public class CraftRotationAction extends CraftBotAction implements RotationAction {
    private final ServerRotationAction serverAction;
    private Consumer<RotationAction> onFail = null;
    private Consumer<RotationAction> onSuccess = null;
    private Consumer<RotationAction> onStop = null;

    public CraftRotationAction(ServerRotationAction serverAction) {
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
    public void setOnFail(Consumer<RotationAction> onFail) {
        this.onFail = onFail;
        serverAction.setOnFail(it -> onFail.accept(new CraftRotationAction(it)));
    }

    @Override
    public Consumer<RotationAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<RotationAction> onSuccess) {
        this.onSuccess = onSuccess;
        serverAction.setOnSuccess(it -> onSuccess.accept(new CraftRotationAction(it)));
    }

    @Override
    public Consumer<RotationAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<RotationAction> onStop) {
        this.onStop = onStop;
        serverAction.setOnStop(it -> onStop.accept(new CraftRotationAction(it)));
    }

    @Override
    public Consumer<RotationAction> getOnStop() {
        return onStop;
    }

    @Override
    public RotationAction setYaw(float yaw) {
        serverAction.setYaw(yaw);
        return this;
    }

    @Override
    public RotationAction setPitch(float pitch) {
        serverAction.setPitch(pitch);
        return this;
    }

    @Override
    public float getYaw() {
        return serverAction.getYaw();
    }

    @Override
    public float getPitch() {
        return serverAction.getPitch();
    }
}
