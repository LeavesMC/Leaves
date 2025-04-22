package org.leavesmc.leaves.protocol.servux.litematics.utils;

import net.minecraft.core.Vec3i;

public record IntBoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    public boolean containsPos(Vec3i pos) {
        return pos.getX() >= this.minX && pos.getX() <= this.maxX && pos.getZ() >= this.minZ && pos.getZ() <= this.maxZ && pos.getY() >= this.minY && pos.getY() <= this.maxY;
    }

    public boolean intersects(IntBoundingBox box) {
        return this.maxX >= box.minX && this.minX <= box.maxX && this.maxZ >= box.minZ && this.minZ <= box.maxZ && this.maxY >= box.minY && this.minY <= box.maxY;
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
}
