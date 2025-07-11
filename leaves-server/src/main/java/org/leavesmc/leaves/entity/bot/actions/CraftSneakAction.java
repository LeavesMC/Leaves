package org.leavesmc.leaves.entity.bot.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.ServerSneakAction;
import org.leavesmc.leaves.entity.bot.action.SneakAction;

import java.util.UUID;
import java.util.function.Consumer;

public class CraftSneakAction implements SneakAction {
    private final ServerSneakAction serverAction;
    private Consumer<SneakAction> onFail = null;
    private Consumer<SneakAction> onSuccess = null;
    private Consumer<SneakAction> onStop = null;

    public CraftSneakAction(ServerSneakAction serverAction) {
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
    public void setOnFail(Consumer<SneakAction> onFail) {
        this.onFail = onFail;
        serverAction.setOnFail(it -> onFail.accept(new CraftSneakAction(it)));
    }

    @Override
    public Consumer<SneakAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<SneakAction> onSuccess) {
        this.onSuccess = onSuccess;
        serverAction.setOnSuccess(it -> onSuccess.accept(new CraftSneakAction(it)));
    }

    @Override
    public Consumer<SneakAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<SneakAction> onStop) {
        this.onStop = onStop;
        serverAction.setOnStop(it -> onStop.accept(new CraftSneakAction(it)));
    }

    @Override
    public Consumer<SneakAction> getOnStop() {
        return onStop;
    }
}
