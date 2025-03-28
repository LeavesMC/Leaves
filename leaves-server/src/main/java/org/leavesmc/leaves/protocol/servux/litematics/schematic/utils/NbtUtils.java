package org.leavesmc.leaves.protocol.servux.litematics.schematic.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import org.leavesmc.leaves.protocol.servux.litematics.ServuxLitematicsProtocol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class NbtUtils {
    public static CompoundTag createBlockPosTag(Vec3i pos) {
        return writeBlockPosToTag(pos, new CompoundTag());
    }

    public static CompoundTag writeBlockPosToTag(Vec3i pos, CompoundTag tag) {
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
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

    public static CompoundTag writeVec3ToTag(Vec3 vec, CompoundTag tag) {
        tag.putDouble("dx", vec.x);
        tag.putDouble("dy", vec.y);
        tag.putDouble("dz", vec.z);
        return tag;
    }

    public static CompoundTag writeEntityPositionToTag(Vec3 pos, CompoundTag tag) {
        ListTag posList = new ListTag();

        posList.add(DoubleTag.valueOf(pos.x));
        posList.add(DoubleTag.valueOf(pos.y));
        posList.add(DoubleTag.valueOf(pos.z));
        tag.put("Pos", posList);

        return tag;
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

    @Nullable
    public static CompoundTag readNbtFromFileAsPath(@Nonnull Path file) {
        return readNbtFromFileAsPath(file, NbtAccounter.unlimitedHeap());
    }

    @Nullable
    public static CompoundTag readNbtFromFileAsPath(@Nonnull Path file, NbtAccounter tracker) {
        if (!Files.exists(file) || !Files.isReadable(file)) {
            return null;
        }

        try {
            return NbtIo.readCompressed(Files.newInputStream(file), tracker);
        } catch (Exception e) {
            ServuxLitematicsProtocol.LOGGER.warn("readNbtFromFileAsPath: Failed to read NBT data from file '{}'", file.toString());
        }

        return null;
    }

    /**
     * Write the compound tag, gzipped, to the output stream.
     */
    public static void writeCompressed(@Nonnull CompoundTag tag, @Nonnull OutputStream outputStream) {
        try {
            NbtIo.writeCompressed(tag, outputStream);
        } catch (Exception err) {
            ServuxLitematicsProtocol.LOGGER.warn("writeCompressed: Failed to write NBT data to output stream");
        }
    }

    public static void writeCompressed(@Nonnull CompoundTag tag, @Nonnull Path file) {
        try {
            NbtIo.writeCompressed(tag, file);
        } catch (Exception err) {
            ServuxLitematicsProtocol.LOGGER.warn("writeCompressed: Failed to write NBT data to file");
        }
    }
}
