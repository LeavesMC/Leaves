package org.leavesmc.leaves.entity.bot;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.entity.bot.action.BotAction;

import java.util.UUID;

/**
 * Represents a fakeplayer
 */
public interface Bot extends Player {

    /**
     * Gets the fakeplayer skin
     *
     * @return fakeplayer skin name
     */
    @Nullable String getSkinName();

    /**
     * Gets the fakeplayer name without prefix and suffix
     *
     * @return fakeplayer raw name
     */
    @NotNull String getRawName();

    /**
     * Gets the creator's UUID of the fakeplayer
     *
     * @return creator's UUID
     */
    @Nullable UUID getCreatePlayerUUID();

    /**
     * Add an action to the fakeplayer
     *
     * @param action bot action
     */
    <T extends BotAction<T>> void addAction(@NotNull T action);

    /**
     * Get the copy action in giving index
     *
     * @param index index of actions
     * @return Action of that index
     */
    BotAction<?> getAction(int index);

    /**
     * Get action size
     *
     * @return size
     */
    int getActionSize();

    /**
     * Stop the action in giving index
     *
     * @param index index of actions
     */
    void stopAction(int index);

    /**
     * Stop all the actions of the fakeplayer
     */
    void stopAllActions();

    /**
     * Remove the fakeplayer
     *
     * @param save should save
     * @return success
     */
    boolean remove(boolean save);

    /**
     * Remove the fakeplayer
     *
     * @param save should save
     * @param resume should resume at next server start
     * @return success
     */
    boolean remove(boolean save, boolean resume);
}
