package org.leavesmc.leaves.protocol.servux.litematics.malilib;

import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class IntBoundingBox {
    public final int minX;
    public final int minY;
    public final int minZ;
    public final int maxX;
    public final int maxY;
    public final int maxZ;

    public IntBoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public boolean containsPos(Vec3i pos) {
        return pos.getX() >= this.minX && pos.getX() <= this.maxX && pos.getZ() >= this.minZ && pos.getZ() <= this.maxZ && pos.getY() >= this.minY && pos.getY() <= this.maxY;
    }

    public boolean containsPos(long pos) {
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        return x >= this.minX && y >= this.minY && z >= this.minZ && x <= this.maxX && y <= this.maxY && z <= this.maxZ;
    }

    public boolean intersects(IntBoundingBox box) {
        return this.maxX >= box.minX && this.minX <= box.maxX && this.maxZ >= box.minZ && this.minZ <= box.maxZ && this.maxY >= box.minY && this.minY <= box.maxY;
    }

    public int getMinValueForAxis(Direction.Axis axis) {
        switch (axis) {
            case X -> {
                return this.minX;
            }
            case Y -> {
                return this.minY;
            }
            case Z -> {
                return this.minZ;
            }
            default -> {
                return 0;
            }
        }
    }

    public int getMaxValueForAxis(Direction.Axis axis) {
        switch (axis) {
            case X -> {
                return this.maxX;
            }
            case Y -> {
                return this.maxY;
            }
            case Z -> {
                return this.maxZ;
            }
            default -> {
                return 0;
            }
        }
    }

    public BlockBox toVanillaBox() {
        return new BlockBox(new BlockPos(this.minX, this.minY, this.minZ), new BlockPos(this.maxX, this.maxY, this.maxZ));
    }

    public IntArrayTag toNBTIntArray() {
        return new IntArrayTag(new int[]{this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ});
    }

    public static IntBoundingBox fromVanillaBox(BlockBox box) {
        BlockPos min = box.min();
        BlockPos max = box.max();
        return createProper(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    public static IntBoundingBox createProper(int x1, int y1, int z1, int x2, int y2, int z2) {
        return new IntBoundingBox(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2), Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    public static IntBoundingBox createForWorldBounds(@Nullable Level world) {
        int worldMinH = -30000000;
        int worldMaxH = 30000000;
        int worldMinY = world != null ? world.getMinY() : -64;
        int worldMaxY = world != null ? world.getMaxY() : 319;
        return new IntBoundingBox(worldMinH, worldMinY, worldMinH, worldMaxH, worldMaxY, worldMaxH);
    }

    public static IntBoundingBox fromArray(int[] coords) {
        return coords.length == 6 ? new IntBoundingBox(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]) : new IntBoundingBox(0, 0, 0, 0, 0, 0);
    }

    public IntBoundingBox expand(int amount) {
        return this.expand(amount, amount, amount);
    }

    public IntBoundingBox expand(int x, int y, int z) {
        return new IntBoundingBox(this.minX - x, this.minY - y, this.minZ - z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public IntBoundingBox shrink(int x, int y, int z) {
        return this.expand(-x, -y, -z);
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.maxX;
        result = 31 * result + this.maxY;
        result = 31 * result + this.maxZ;
        result = 31 * result + this.minX;
        result = 31 * result + this.minY;
        result = 31 * result + this.minZ;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            IntBoundingBox other = (IntBoundingBox) obj;
            return this.maxX == other.maxX && this.maxY == other.maxY && this.maxZ == other.maxZ && this.minX == other.minX && this.minY == other.minY && this.minZ == other.minZ;
        } else {
            return false;
        }
    }
}
