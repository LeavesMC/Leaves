package org.leavesmc.leaves.entity;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.bot.agent.actions.CraftBotAction;
import org.leavesmc.leaves.bot.agent.actions.CraftCustomBotAction;
import org.leavesmc.leaves.bot.agent.actions.CraftCustomStateBotAction;
import org.leavesmc.leaves.bot.agent.actions.CraftCustomTimerBotAction;
import org.leavesmc.leaves.entity.bot.Bot;
import org.leavesmc.leaves.entity.bot.action.BotAction;
import org.leavesmc.leaves.entity.bot.action.CustomBotAction;
import org.leavesmc.leaves.entity.bot.action.CustomStateBotAction;
import org.leavesmc.leaves.entity.bot.action.CustomTimerBotAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;
import org.leavesmc.leaves.event.bot.BotRemoveEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class CraftBot extends CraftPlayer implements Bot {

    public CraftBot(CraftServer server, ServerBot entity) {
        super(server, entity);
    }

    @Override
    public String getSkinName() {
        return this.getHandle().createState.skinName();
    }

    @Override
    public @NotNull String getRealName() {
        return this.getHandle().createState.realName();
    }

    @Override
    public @Nullable UUID getCreatePlayerUUID() {
        return this.getHandle().createPlayer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BotAction<T>> void addAction(@NotNull T action) {
        CraftBotAction<?> result;
        Supplier<T> getRegAction = () -> (T) Actions.getForClass(action.getClass());
        switch (action) {
            case CustomBotAction act -> {
                T regAction = getRegAction.get();
                if (regAction == null) throw new IllegalStateException("Action " + action.getClass().getName() + " is not registered!");
                result = new CraftCustomBotAction(regAction.getName(), act);
            }
            case CustomTimerBotAction act -> {
                T regAction = getRegAction.get();
                if (regAction == null) throw new IllegalStateException("Action " + action.getClass().getName() + " is not registered!");
                result = new CraftCustomTimerBotAction(regAction.getName(), act);
            }
            case CustomStateBotAction act -> {
                T regAction = getRegAction.get();
                if (regAction == null) throw new IllegalStateException("Action " + action.getClass().getName() + " is not registered!");
                result = new CraftCustomStateBotAction(regAction.getName(), act);
            }
            case CraftBotAction<?> craftBotAction -> result = craftBotAction;
            default -> throw new IllegalArgumentException("Action " + action.getClass().getName() + " is not a valid BotAction type!");
        }
        this.getHandle().addBotAction(result, null);
    }

    @Override
    public BotAction<?> getAction(int index) {
        return this.getHandle().getBotActions().get(index);
    }

    @Override
    public int getActionSize() {
        return this.getHandle().getBotActions().size();
    }

    @Override
    public void stopAction(int index) {
        this.getHandle().getBotActions().get(index).stop(this.getHandle(), BotActionStopEvent.Reason.PLUGIN);
    }

    @Override
    public void stopAllActions() {
        for (CraftBotAction<?> action : this.getHandle().getBotActions()) {
            action.stop(this.getHandle(), BotActionStopEvent.Reason.PLUGIN);
        }
    }

    @Override
    public boolean remove(boolean save) {
        BotList.INSTANCE.removeBot(this.getHandle(), BotRemoveEvent.RemoveReason.PLUGIN, null, save);
        return true;
    }

    @Override
    public boolean teleport(Location location, PlayerTeleportEvent.@NotNull TeleportCause cause, io.papermc.paper.entity.TeleportFlag @NotNull ... flags) {
        Preconditions.checkArgument(location != null, "location cannot be null");
        Preconditions.checkState(location.getWorld().equals(this.getWorld()), "[Leaves] Fakeplayers do not support changing world, Please use leaves fakeplayer-api instead!");
        return super.teleport(location, cause, flags);
    }

    @Override
    public ServerBot getHandle() {
        return (ServerBot) entity;
    }

    public void setHandle(final ServerBot entity) {
        super.setHandle(entity);
    }

    @Override
    public String toString() {
        return "CraftBot{" + "name=" + getName() + '}';
    }
}
