package org.leavesmc.leaves.entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface BotCreator {

    default BotCreator of(String realName, Location location) {
        return Bukkit.getBotManager().botCreator(realName, location);
    }

    public BotCreator name(String name);

    public BotCreator skinName(String skinName);

    public BotCreator skin(String[] skin);

    /**
     * Sets the skin of the bot using the Mojang API based on the provided skin name.
     * <p>
     * Need Async.
     *
     * @return BotCreator
     */
    public BotCreator mojangAPISkin();

    public BotCreator location(@NotNull Location location);

    public BotCreator creator(@Nullable CommandSender creator);

    /**
     * Create a bot directly
     *
     * @return a bot, null spawn fail
     */
    @Nullable
    public Bot spawn();

    /**
     * Create a bot and apply skin of player names `skinName` from MojangAPI
     * just like `mojangAPISkin().spawn()`, but async
     * <p>
     * you can not get the bot instance instantly because get skin in on async thread
     *
     * @param consumer Consumer
     */
    public void spawnWithSkin(Consumer<Bot> consumer);
}
