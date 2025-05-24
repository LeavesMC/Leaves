package org.leavesmc.leaves.protocol.servux.litematics.container;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class LitematicaBlockStateContainer {

    public static final BlockState AIR_BLOCK_STATE = Blocks.AIR.defaultBlockState();
    protected final Vec3i size;
    protected final int sizeX;
    protected final int sizeY;
    protected final int sizeZ;
    protected final int sizeLayer;
    protected final long totalVolume;
    protected LitematicaBitArray storage;
    protected LitematicaBlockStatePalette palette;
    protected int bits;

    public LitematicaBlockStateContainer(int sizeX, int sizeY, int sizeZ, int bits, @Nullable long[] backingLongArray) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.sizeLayer = sizeX * sizeZ;
        this.totalVolume = (long) this.sizeX * (long) this.sizeY * (long) this.sizeZ;
        this.size = new Vec3i(this.sizeX, this.sizeY, this.sizeZ);

        this.setBits(bits, backingLongArray);
    }

    public static LitematicaBlockStateContainer createFrom(ListTag palette, long[] blockStates, BlockPos size) {
        int bits = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(palette.size() - 1));
        LitematicaBlockStateContainer container = new LitematicaBlockStateContainer(size.getX(), size.getY(), size.getZ(), bits, blockStates);
        container.palette.readFromNBT(palette);
        return container;
    }

    public Vec3i getSize() {
        return this.size;
    }

    public LitematicaBitArray getArray() {
        return this.storage;
    }

    public BlockState get(int x, int y, int z) {
        BlockState state = this.palette.getBlockState(this.storage.getAt(this.getIndex(x, y, z)));
        return state == null ? AIR_BLOCK_STATE : state;
    }

    protected int getIndex(int x, int y, int z) {
        return (y * this.sizeLayer) + z * this.sizeX + x;
    }

    protected void setBits(int bitsIn, @Nullable long[] backingLongArray) {
        if (bitsIn != this.bits) {
            this.bits = bitsIn;

            if (this.bits <= 4) {
                this.bits = Math.max(2, this.bits);
                this.palette = new LitematicaBlockStatePaletteLinear(this.bits, this);
            } else {
                this.palette = new LitematicaBlockStatePaletteHashMap(this.bits, this);
            }

            this.palette.idFor(AIR_BLOCK_STATE);

            if (backingLongArray != null) {
                this.storage = new LitematicaBitArray(this.bits, this.totalVolume, backingLongArray);
            } else {
                this.storage = new LitematicaBitArray(this.bits, this.totalVolume);
            }
        }
    }

    public int onResize(int bits, BlockState state) {
        LitematicaBitArray oldStorage = this.storage;
        LitematicaBlockStatePalette oldPalette = this.palette;
        final long storageLength = oldStorage.size();

        this.setBits(bits, null);

        LitematicaBitArray newStorage = this.storage;

        for (long index = 0; index < storageLength; ++index) {
            newStorage.setAt(index, oldStorage.getAt(index));
        }

        this.palette.readFromNBT(oldPalette.writeToNBT());

        return this.palette.idFor(state);
    }

    public LitematicaBlockStatePalette getPalette() {
        return this.palette;
    }
}