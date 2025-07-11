package org.leavesmc.leaves.entity.bot.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.ServerBotAction;
import org.leavesmc.leaves.bot.agent.actions.ServerSwimAction;
import org.leavesmc.leaves.entity.bot.action.SwimAction;

import java.util.UUID;
import java.util.function.Consumer;

public class CraftSwimAction extends CraftBotAction implements SwimAction {
    private final ServerSwimAction serverAction;
    private Consumer<SwimAction> onFail = null;
    private Consumer<SwimAction> onSuccess = null;
    private Consumer<SwimAction> onStop = null;

    public CraftSwimAction(ServerSwimAction serverAction) {
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
    public void setOnFail(Consumer<SwimAction> onFail) {
        this.onFail = onFail;
        serverAction.setOnFail(it -> onFail.accept(new CraftSwimAction(it)));
    }

    @Override
    public Consumer<SwimAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<SwimAction> onSuccess) {
        this.onSuccess = onSuccess;
        serverAction.setOnSuccess(it -> onSuccess.accept(new CraftSwimAction(it)));
    }

    @Override
    public Consumer<SwimAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<SwimAction> onStop) {
        this.onStop = onStop;
        serverAction.setOnStop(it -> onStop.accept(new CraftSwimAction(it)));
    }

    @Override
    public Consumer<SwimAction> getOnStop() {
        return onStop;
    }
}
