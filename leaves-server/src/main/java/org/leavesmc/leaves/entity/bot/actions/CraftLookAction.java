package org.leavesmc.leaves.entity.bot.actions;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.ServerBotAction;
import org.leavesmc.leaves.bot.agent.actions.ServerLookAction;
import org.leavesmc.leaves.entity.bot.action.LookAction;

import java.util.UUID;
import java.util.function.Consumer;

public class CraftLookAction extends CraftBotAction implements LookAction {
    private final ServerLookAction serverAction;
    private Consumer<LookAction> onFail = null;
    private Consumer<LookAction> onSuccess = null;
    private Consumer<LookAction> onStop = null;

    public CraftLookAction(ServerLookAction serverAction) {
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
    public void setOnFail(Consumer<LookAction> onFail) {
        this.onFail = onFail;
        serverAction.setOnFail(it -> onFail.accept(new CraftLookAction(it)));
    }

    @Override
    public Consumer<LookAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<LookAction> onSuccess) {
        this.onSuccess = onSuccess;
        serverAction.setOnSuccess(it -> onSuccess.accept(new CraftLookAction(it)));
    }

    @Override
    public Consumer<LookAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<LookAction> onStop) {
        this.onStop = onStop;
        serverAction.setOnStop(it -> onStop.accept(new CraftLookAction(it)));
    }

    @Override
    public Consumer<LookAction> getOnStop() {
        return onStop;
    }

    @Override
    public LookAction setPos(Vector pos) {
        serverAction.setPos(pos);
        return this;
    }

    @Override
    public Vector getPos() {
        return serverAction.getPos();
    }

    @Override
    public LookAction setTarget(Player player) {
        serverAction.setTarget(player);
        return this;
    }

    @Override
    public Player getTarget() {
        return serverAction.getTarget();
    }
}
