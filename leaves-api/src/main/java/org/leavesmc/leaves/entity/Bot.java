package org.leavesmc.leaves.entity;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.entity.botaction.LeavesBotAction;

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
    @Nullable
    public String getSkinName();

    /**
     * Gets the fakeplayer name without prefix and suffix
     *
     * @return fakeplayer real name
     */
    @NotNull
    public String getRealName();

    /**
     * Gets the creator's UUID of the fakeplayer
     *
     * @return creator's UUID
     */
    @Nullable
    public UUID getCreatePlayerUUID();

    /**
     * Add an action to the fakeplayer
     *
     * @param action bot action
     */
    public void addAction(@NotNull LeavesBotAction action);

    /**
     * Get the copy action in giving index
     *
     * @param index index of actions
     * @return Action of that index
     */
    public LeavesBotAction getAction(int index);

    /**
     * Get action size
     *
     * @return size
     */
    public int getActionSize();

    /**
     * Stop the action in giving index
     *
     * @param index index of actions
     */
    public void stopAction(int index);

    /**
     * Stop all the actions of the fakeplayer
     */
    public void stopAllActions();

    /**
     * Remove the fakeplayer
     *
     * @param save should save
     * @return success
     */
    public boolean remove(boolean save);
}
