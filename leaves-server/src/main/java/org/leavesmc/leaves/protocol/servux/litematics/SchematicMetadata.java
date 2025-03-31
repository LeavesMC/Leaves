package org.leavesmc.leaves.protocol.servux.litematics;

import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.leavesmc.leaves.protocol.servux.litematics.utils.FileType;
import org.leavesmc.leaves.protocol.servux.litematics.utils.NbtUtils;
import org.leavesmc.leaves.protocol.servux.litematics.utils.Schema;

import javax.annotation.Nullable;

public class SchematicMetadata {
    private String name = "?";
    private String author = "?";
    private String description = "";
    private Vec3i enclosingSize = Vec3i.ZERO;
    private long timeCreated;
    private long timeModified;
    protected int minecraftDataVersion;
    protected int schematicVersion;
    protected Schema schema;
    protected FileType type;
    private int regionCount;
    private int totalVolume = -1;
    private int totalBlocks = -1;
    @Nullable
    protected int[] thumbnailPixelData;

    public String getName() {
        return this.name;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getDescription() {
        return this.description;
    }
    public Schema getSchema() {
        return this.schema;
    }

    public FileType getFileType() {
        if (this.type != null) {
            return this.type;
        }

        return FileType.UNKNOWN;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSchematicVersion(int version) {
        this.schematicVersion = version;
    }

    public void setMinecraftDataVersion(int minecraftDataVersion) {
        this.minecraftDataVersion = minecraftDataVersion;
        this.schema = Schema.getSchemaByDataVersion(this.minecraftDataVersion);
    }
    public void setFileType(FileType type) {
        this.type = type;
    }

    public void copyFrom(SchematicMetadata other) {
        this.name = other.name;
        this.author = other.author;
        this.description = other.description;
        this.enclosingSize = other.enclosingSize;
        this.timeCreated = other.timeCreated;
        this.timeModified = other.timeModified;
        this.regionCount = other.regionCount;
        this.totalVolume = other.totalVolume;
        this.totalBlocks = other.totalBlocks;

        this.schematicVersion = other.schematicVersion;
        this.minecraftDataVersion = other.minecraftDataVersion;
        this.schema = Schema.getSchemaByDataVersion(other.minecraftDataVersion);
        this.type = other.getFileType();

        if (other.thumbnailPixelData != null) {
            this.thumbnailPixelData = new int[other.thumbnailPixelData.length];
            System.arraycopy(other.thumbnailPixelData, 0, this.thumbnailPixelData, 0, this.thumbnailPixelData.length);
        } else {
            this.thumbnailPixelData = null;
        }
    }
    public void readFromNBT(CompoundTag nbt) {
        this.name = nbt.getString("Name");
        this.author = nbt.getString("Author");
        this.description = nbt.getString("Description");
        this.regionCount = nbt.getInt("RegionCount");
        this.timeCreated = nbt.getLong("TimeCreated");
        this.timeModified = nbt.getLong("TimeModified");

        if (nbt.contains("TotalVolume", Tag.TAG_ANY_NUMERIC)) {
            this.totalVolume = nbt.getInt("TotalVolume");
        }

        if (nbt.contains("TotalBlocks", Tag.TAG_ANY_NUMERIC)) {
            this.totalBlocks = nbt.getInt("TotalBlocks");
        }

        if (nbt.contains("EnclosingSize", Tag.TAG_COMPOUND)) {
            Vec3i size = NbtUtils.readVec3iFromTag(nbt.getCompound("EnclosingSize"));

            if (size != null) {
                this.enclosingSize = size;
            }
        }

        if (nbt.contains("PreviewImageData", Tag.TAG_INT_ARRAY)) {
            this.thumbnailPixelData = nbt.getIntArray("PreviewImageData");
        } else {
            this.thumbnailPixelData = null;
        }
    }
}
