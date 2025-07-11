package org.leavesmc.leaves.entity.bot.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.ServerBotAction;
import org.leavesmc.leaves.bot.agent.actions.ServerMoveAction;
import org.leavesmc.leaves.entity.bot.action.MoveAction;

import java.util.UUID;
import java.util.function.Consumer;

public class CraftMoveAction extends CraftBotAction implements MoveAction {
    private final ServerMoveAction serverAction;
    private Consumer<MoveAction> onFail = null;
    private Consumer<MoveAction> onSuccess = null;
    private Consumer<MoveAction> onStop = null;

    public CraftMoveAction(ServerMoveAction serverAction) {
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
    public void setOnFail(Consumer<MoveAction> onFail) {
        this.onFail = onFail;
        serverAction.setOnFail(it -> onFail.accept(new CraftMoveAction(it)));
    }

    @Override
    public Consumer<MoveAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<MoveAction> onSuccess) {
        this.onSuccess = onSuccess;
        serverAction.setOnSuccess(it -> onSuccess.accept(new CraftMoveAction(it)));
    }

    @Override
    public Consumer<MoveAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<MoveAction> onStop) {
        this.onStop = onStop;
        serverAction.setOnStop(it -> onStop.accept(new CraftMoveAction(it)));
    }

    @Override
    public Consumer<MoveAction> getOnStop() {
        return onStop;
    }

    @Override
    public MoveDirection getDirection() {
        return serverAction.getDirection();
    }

    @Override
    public MoveAction setDirection(MoveDirection direction) {
        serverAction.setDirection(direction);
        return this;
    }
}
