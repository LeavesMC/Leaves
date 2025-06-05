package org.leavesmc.leaves.protocol.servux.litematics.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;
import org.leavesmc.leaves.protocol.servux.litematics.utils.Int2ObjectBiMap;

import javax.annotation.Nullable;

public class LitematicaBlockStatePaletteHashMap implements LitematicaBlockStatePalette {

    private final Int2ObjectBiMap<BlockState> statePaletteMap;
    private final LitematicaBlockStateContainer blockStateContainer;
    private final int bits;

    public LitematicaBlockStatePaletteHashMap(int bitsIn, LitematicaBlockStateContainer blockStateContainer) {
        this.bits = bitsIn;
        this.blockStateContainer = blockStateContainer;
        this.statePaletteMap = Int2ObjectBiMap.create(1 << bitsIn);
    }

    @Override
    public int idFor(BlockState state) {
        int i = this.statePaletteMap.getRawId(state);

        if (i == -1) {
            i = this.statePaletteMap.add(state);

            if (i >= (1 << this.bits)) {
                i = this.blockStateContainer.onResize(this.bits + 1, state);
            }
        }

        return i;
    }

    @Override
    @Nullable
    public BlockState getBlockState(int indexKey) {
        return this.statePaletteMap.get(indexKey);
    }

    public void requestNewId(BlockState state) {
        final int origId = this.statePaletteMap.add(state);

        if (origId >= (1 << this.bits)) {
            int newId = this.blockStateContainer.onResize(this.bits + 1, LitematicaBlockStateContainer.AIR_BLOCK_STATE);

            if (newId <= origId) {
                this.statePaletteMap.add(state);
            }
        }
    }

    @Override
    public ListTag writeToNBT() {
        ListTag tagList = new ListTag();

        for (int id = 0; id < this.statePaletteMap.size(); ++id) {
            BlockState state = this.statePaletteMap.get(id);

            if (state == null) {
                state = LitematicaBlockStateContainer.AIR_BLOCK_STATE;
            }

            CompoundTag tag = NbtUtils.writeBlockState(state);
            tagList.add(tag);
        }

        return tagList;
    }
}