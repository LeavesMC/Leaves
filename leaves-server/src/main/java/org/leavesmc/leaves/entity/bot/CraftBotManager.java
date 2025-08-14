package org.leavesmc.leaves.entity.bot;

import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.BotCreateState;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.bot.agent.actions.ServerBotAction;
import org.leavesmc.leaves.entity.bot.action.BotAction;
import org.leavesmc.leaves.event.bot.BotCreateEvent;

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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BotAction<T>> T newAction(@NotNull Class<T> type) {
        ServerBotAction<?> action = Actions.getForClass(type);
        if (action == null) {
            throw new IllegalArgumentException("No action registered for type: " + type.getName());
        } else {
            try {
                return (T) action.create().asCraft();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create action of type: " + type.getName(), e);
            }
        }
    }

    @Override
    public BotCreator botCreator(@NotNull String realName, @NotNull Location location) {
        return BotCreateState.builder(realName, location).createReason(BotCreateEvent.CreateReason.PLUGIN);
    }
}
