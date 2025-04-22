package org.leavesmc.leaves.protocol.servux.litematics.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class NbtUtils {

    public static void writeBlockPosToTag(Vec3i pos, CompoundTag tag) {
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
    }

    @Nullable
    public static BlockPos readBlockPos(@Nullable CompoundTag tag) {
        if (tag != null &&
            tag.contains("x", Tag.TAG_INT) &&
            tag.contains("y", Tag.TAG_INT) &&
            tag.contains("z", Tag.TAG_INT)) {
            return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        }

        return null;
    }

    public static void writeEntityPositionToTag(Vec3 pos, CompoundTag tag) {
        ListTag posList = new ListTag();

        posList.add(DoubleTag.valueOf(pos.x));
        posList.add(DoubleTag.valueOf(pos.y));
        posList.add(DoubleTag.valueOf(pos.z));
        tag.put("Pos", posList);
    }

    @Nullable
    public static Vec3 readVec3(@Nullable CompoundTag tag) {
        if (tag != null &&
            tag.contains("dx", Tag.TAG_DOUBLE) &&
            tag.contains("dy", Tag.TAG_DOUBLE) &&
            tag.contains("dz", Tag.TAG_DOUBLE)) {
            return new Vec3(tag.getDouble("dx"), tag.getDouble("dy"), tag.getDouble("dz"));
        }

        return null;
    }

    @Nullable
    public static Vec3 readEntityPositionFromTag(@Nullable CompoundTag tag) {
        if (tag != null && tag.contains("Pos", Tag.TAG_LIST)) {
            ListTag tagList = tag.getList("Pos", Tag.TAG_DOUBLE);

            if (tagList.getElementType() == Tag.TAG_DOUBLE && tagList.size() == 3) {
                return new Vec3(tagList.getDouble(0), tagList.getDouble(1), tagList.getDouble(2));
            }
        }

        return null;
    }

    @Nullable
    public static Vec3i readVec3iFromTag(@Nullable CompoundTag tag) {
        if (tag != null &&
            tag.contains("x", Tag.TAG_INT) &&
            tag.contains("y", Tag.TAG_INT) &&
            tag.contains("z", Tag.TAG_INT)) {
            return new Vec3i(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        }

        return null;
    }
}
