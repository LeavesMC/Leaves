package org.leavesmc.leaves.protocol.servux.litematics.container;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface ILitematicaBlockStatePalette {
    /**
     * Gets the palette id for the given block state and adds
     * the state to the palette if it doesn't exist there yet.
     */
    int idFor(BlockState state);

    /**
     * Gets the block state by the palette id.
     */
    @Nullable
    BlockState getBlockState(int indexKey);

    void readFromNBT(ListTag tagList);

    ListTag writeToNBT();
}
