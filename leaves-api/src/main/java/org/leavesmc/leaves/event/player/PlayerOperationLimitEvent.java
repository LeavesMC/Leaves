package org.leavesmc.leaves.event.player;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player operation is limited
 */
public class PlayerOperationLimitEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Block block;
    private final Operation operation;

    public PlayerOperationLimitEvent(@NotNull Player who, Operation operation, Block block) {
        super(who);
        this.block = block;
        this.operation = operation;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gets the operated block
     *
     * @return block
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Gets the type of operation
     *
     * @return operation type
     */
    public Operation getOperation() {
        return operation;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public enum Operation {
        MINE, PLACE
    }
}
