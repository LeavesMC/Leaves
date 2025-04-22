package org.leavesmc.leaves.protocol.servux.litematics;

import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.servux.litematics.utils.FileType;
import org.leavesmc.leaves.protocol.servux.litematics.utils.NbtUtils;
import org.leavesmc.leaves.protocol.servux.litematics.utils.Schema;

import javax.annotation.Nullable;
import java.util.Objects;

public record SchematicMetadata(
    String name,
    String author,
    String description,
    Vec3i enclosingSize,
    long timeCreated,
    long timeModified,
    int minecraftDataVersion,
    int schematicVersion,
    Schema schema,
    FileType type,
    int regionCount,
    long totalVolume,
    long totalBlocks,
    @Nullable int[] thumbnailPixelData
) {
    @NotNull
    @Contract("_, _, _, _ -> new")
    public static SchematicMetadata readFromNBT(@NotNull CompoundTag nbt, int version, int minecraftDataVersion, FileType fileType) {
        String name = nbt.getString("Name");
        String author = nbt.getString("Author");
        String description = nbt.getString("Description");
        int regionCount = nbt.getInt("RegionCount");
        long timeCreated = nbt.getLong("TimeCreated");
        long timeModified = nbt.getLong("TimeModified");

        long totalVolume = -1;
        if (nbt.contains("TotalVolume", Tag.TAG_ANY_NUMERIC)) {
            totalVolume = nbt.getInt("TotalVolume");
        }

        long totalBlocks = -1;
        if (nbt.contains("TotalBlocks", Tag.TAG_ANY_NUMERIC)) {
            totalBlocks = nbt.getInt("TotalBlocks");
        }

        Vec3i enclosingSize = Vec3i.ZERO;
        if (nbt.contains("EnclosingSize", Tag.TAG_COMPOUND)) {
            enclosingSize = Objects.requireNonNullElse(NbtUtils.readVec3iFromTag(nbt.getCompound("EnclosingSize")), Vec3i.ZERO);
        }

        int[] thumbnailPixelData = null;
        if (nbt.contains("PreviewImageData", Tag.TAG_INT_ARRAY)) {
            thumbnailPixelData = nbt.getIntArray("PreviewImageData");
        }

        return new SchematicMetadata(name, author, description, enclosingSize, timeCreated, timeModified, minecraftDataVersion, version, Schema.getSchemaByDataVersion(minecraftDataVersion), fileType, regionCount, totalVolume, totalBlocks, thumbnailPixelData);
    }
}
