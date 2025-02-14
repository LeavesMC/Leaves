package org.leavesmc.leaves.entity;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.BotCreateState;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.bot.agent.BotAction;
import org.leavesmc.leaves.bot.agent.actions.CraftCustomBotAction;
import org.leavesmc.leaves.entity.botaction.CustomBotAction;
import org.leavesmc.leaves.event.bot.BotCreateEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class CraftBotManager implements BotManager {

    private final BotList botList;
    private final Collection<Bot> botViews;

    public CraftBotManager() {
        this.botList = MinecraftServer.getServer().getBotList();
        this.botViews = Collections.unmodifiableList(Lists.transform(botList.bots, new Function<ServerBot, CraftBot>() {
            @Override
            public CraftBot apply(ServerBot bot) {
                return bot.getBukkitEntity();
            }
        }));
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
    public boolean registerCustomBotAction(String name, CustomBotAction action) {
        return Actions.register(new CraftCustomBotAction(name, action));
    }

    @Override
    public boolean unregisterCustomBotAction(String name) {
        BotAction<?> action = Actions.getForName(name);
        if (action instanceof CraftCustomBotAction) {
            return Actions.unregister(name);
        }
        return false;
    }

    @Override
    public BotCreator botCreator(@NotNull String realName, @NotNull Location location) {
        return BotCreateState.builder(realName, location).createReason(BotCreateEvent.CreateReason.PLUGIN);
    }
}
