package org.leavesmc.leaves.entity;

import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.BotCreateState;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.bot.agent.actions.CraftBotAction;
import org.leavesmc.leaves.bot.agent.actions.CraftCustomAction;
import org.leavesmc.leaves.bot.agent.actions.CraftCustomBotAction;
import org.leavesmc.leaves.bot.agent.actions.CraftCustomStateBotAction;
import org.leavesmc.leaves.bot.agent.actions.CraftCustomTimerBotAction;
import org.leavesmc.leaves.entity.bot.Bot;
import org.leavesmc.leaves.entity.bot.BotCreator;
import org.leavesmc.leaves.entity.bot.BotManager;
import org.leavesmc.leaves.entity.bot.action.BotAction;
import org.leavesmc.leaves.entity.bot.action.AbstractCustomBotAction;
import org.leavesmc.leaves.entity.bot.action.AbstractCustomStateBotAction;
import org.leavesmc.leaves.entity.bot.action.AbstractCustomTimerBotAction;
import org.leavesmc.leaves.entity.bot.action.CustomBotAction;
import org.leavesmc.leaves.event.bot.BotCreateEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class CraftBotManager implements BotManager {

    private final BotList botList;
    private final Collection<Bot> botViews;

    public CraftBotManager() {
        this.botList = MinecraftServer.getServer().getBotList();
        this.botViews = Collections.unmodifiableList(Lists.transform(botList.bots, ServerBot::getBukkitEntity));
    }

    @Override
    public @Nullable Bot getBot(@NotNull UUID uuid) {
        ServerBot bot = botList.getBot(uuid);
        if (bot != null) {
            return bot.getBukkitEntity();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable Bot getBot(@NotNull String name) {
        ServerBot bot = botList.getBotByName(name);
        if (bot != null) {
            return bot.getBukkitEntity();
        } else {
            return null;
        }
    }

    @Override
    public Collection<Bot> getBots() {
        return botViews;
    }

    @Override
    public boolean registerCustomBotAction(CustomBotAction<?> action) {
        return switch (action) {
            case AbstractCustomBotAction act -> Actions.register(new CraftCustomBotAction(act.getName(), act));
            case AbstractCustomStateBotAction act -> Actions.register(new CraftCustomStateBotAction(act.getName(), act));
            case AbstractCustomTimerBotAction act -> Actions.register(new CraftCustomTimerBotAction(act.getName(), act));
            case CraftCustomBotAction craftAction -> Actions.register(craftAction);
            case null, default -> throw new IllegalArgumentException("Unsupported action type: " + action);
        };
    }

    @Override
    public boolean unregisterCustomBotAction(String name) {
        CraftBotAction<?> action = Actions.getForName(name);
        if (action instanceof CraftCustomAction) {
            return Actions.unregister(name);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BotAction<T>> T newAction(@NotNull Class<T> type) {
        if (type.isAssignableFrom(CustomBotAction.class)) try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        T action = Actions.getForClass(type);
        if (action == null) {
            throw new IllegalArgumentException("No action registered for type: " + type.getName());
        }
        try {
            return (T) ((CraftBotAction<?>) action).create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create action of type: " + type.getName(), e);
        }
    }

    @Override
    public BotCreator botCreator(@NotNull String realName, @NotNull Location location) {
        return BotCreateState.builder(realName, location).createReason(BotCreateEvent.CreateReason.PLUGIN);
    }
}
