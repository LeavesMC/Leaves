package org.leavesmc.leaves.protocol.servux.litematics.container;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface LitematicaBlockStatePalette {
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

    void requestNewId(BlockState state);

    default void readFromNBT(ListTag tagList) {
        Registry<Block> lookup = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.BLOCK);

        final int size = tagList.size();

        for (int i = 0; i < size; ++i) {
            CompoundTag tag = tagList.getCompound(i).orElseThrow();
            BlockState state = NbtUtils.readBlockState(lookup, tag);

            if (i > 0 || state != LitematicaBlockStateContainer.AIR_BLOCK_STATE) {
                this.requestNewId(state);
            }
        }
    }

    ListTag writeToNBT();
}