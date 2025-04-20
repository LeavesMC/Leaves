package org.leavesmc.leaves.protocol.servux.litematics;


import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.leavesmc.leaves.protocol.servux.ServuxProtocol;
import org.leavesmc.leaves.protocol.servux.litematics.container.LitematicaBlockStateContainer;
import org.leavesmc.leaves.protocol.servux.litematics.utils.FileType;
import org.leavesmc.leaves.protocol.servux.litematics.utils.NbtUtils;
import org.leavesmc.leaves.protocol.servux.litematics.utils.PositionUtils;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LitematicaSchematic {

    public static final int MINECRAFT_DATA_VERSION = SharedConstants.getProtocolVersion();
    public static final int SCHEMATIC_VERSION = 7;

    public final Map<String, LitematicaBlockStateContainer> blockContainers = new HashMap<>();
    public final Map<String, Map<BlockPos, CompoundTag>> tileEntities = new HashMap<>();
    public final Map<String, Map<BlockPos, ScheduledTick<Block>>> pendingBlockTicks = new HashMap<>();
    public final Map<String, Map<BlockPos, ScheduledTick<Fluid>>> pendingFluidTicks = new HashMap<>();
    public final Map<String, List<EntityInfo>> entities = new HashMap<>();
    public final Map<String, BlockPos> subRegionPositions = new HashMap<>();
    public final Map<String, BlockPos> subRegionSizes = new HashMap<>();
    public final SchematicMetadata metadata = new SchematicMetadata();
    @Nullable
    private final Path schematicFile;

    public LitematicaSchematic(CompoundTag nbtCompound) throws CommandSyntaxException {
        this.readFromNBT(nbtCompound);
        this.schematicFile = Path.of("/");
    }

    @Nullable
    public Path getFile() {
        return this.schematicFile;
    }

    public SchematicMetadata getMetadata() {
        return this.metadata;
    }

    public Map<String, BlockPos> getAreaSizes() {
        ImmutableMap.Builder<String, BlockPos> builder = ImmutableMap.builder();

        for (String name : this.subRegionSizes.keySet()) {
            BlockPos pos = this.subRegionSizes.get(name);
            builder.put(name, pos);
        }

        return builder.build();
    }

    @Nullable
    public BlockPos getAreaSize(String regionName) {
        return this.subRegionSizes.get(regionName);
    }

    @Nullable
    public LitematicaBlockStateContainer getSubRegionContainer(String regionName) {
        return this.blockContainers.get(regionName);
    }

    @Nullable
    public Map<BlockPos, CompoundTag> getBlockEntityMapForRegion(String regionName) {
        return this.tileEntities.get(regionName);
    }

    @Nullable
    public List<EntityInfo> getEntityListForRegion(String regionName) {
        return this.entities.get(regionName);
    }

    @Nullable
    public Map<BlockPos, ScheduledTick<Block>> getScheduledBlockTicksForRegion(String regionName) {
        return this.pendingBlockTicks.get(regionName);
    }

    @Nullable
    public Map<BlockPos, ScheduledTick<Fluid>> getScheduledFluidTicksForRegion(String regionName) {
        return this.pendingFluidTicks.get(regionName);
    }

    private void readFromNBT(CompoundTag nbt) throws CommandSyntaxException {
        this.blockContainers.clear();
        this.tileEntities.clear();
        this.entities.clear();
        this.pendingBlockTicks.clear();
        this.subRegionPositions.clear();
        this.subRegionSizes.clear();

        if (nbt.contains("Version", Tag.TAG_INT)) {
            final int version = nbt.getInt("Version");
            final int minecraftDataVersion = nbt.contains("MinecraftDataVersion") ? nbt.getInt("MinecraftDataVersion") : SharedConstants.getProtocolVersion();

            if (version >= 1 && version <= SCHEMATIC_VERSION) {
                this.metadata.readFromNBT(nbt.getCompound("Metadata"));
                this.metadata.setSchematicVersion(version);
                this.metadata.setMinecraftDataVersion(minecraftDataVersion);
                this.metadata.setFileType(FileType.LITEMATICA_SCHEMATIC);
                this.readSubRegionsFromNBT(nbt.getCompound("Regions"), version, minecraftDataVersion);

            } else {
                error("Unsupported or future schematic version");
            }
        } else {
            error("The schematic doesn't have version information, and can't be safely loaded!");
        }
    }

    private void error(String s) throws CommandSyntaxException {
        throw new SimpleCommandExceptionType(Component.literal(s)).create();
    }

    private void readSubRegionsFromNBT(CompoundTag tag, int version, int minecraftDataVersion) {
        for (String regionName : tag.getAllKeys()) {
            Tag region = tag.get(regionName);
            if (region == null) throw new RuntimeException("Unknown region: " + regionName);
            if (region.getId() != Tag.TAG_COMPOUND) {
                continue;
            }
            CompoundTag regionTag = tag.getCompound(regionName);
            BlockPos regionPos = NbtUtils.readBlockPos(regionTag.getCompound("Position"));
            BlockPos regionSize = NbtUtils.readBlockPos(regionTag.getCompound("Size"));
            Map<BlockPos, CompoundTag> tiles;
            if (regionPos == null || regionSize == null) {
                continue;
            }
            this.subRegionPositions.put(regionName, regionPos);
            this.subRegionSizes.put(regionName, regionSize);
            if (version >= 2) {
                tiles = this.readTileEntitiesFromNBT(regionTag.getList("TileEntities", Tag.TAG_COMPOUND));
                this.tileEntities.put(regionName, tiles);
                ListTag entities = regionTag.getList("Entities", Tag.TAG_COMPOUND);
                this.entities.put(regionName, this.readEntitiesFromNBT(entities));
            } else if (version == 1) {
                tiles = this.readTileEntitiesFromNBT_v1(regionTag.getList("TileEntities", Tag.TAG_COMPOUND));
                this.tileEntities.put(regionName, tiles);
                this.entities.put(regionName, this.readEntitiesFromNBT_v1(regionTag.getList("Entities", Tag.TAG_COMPOUND)));
            }
            if (version >= 3) {
                ListTag list = regionTag.getList("PendingBlockTicks", Tag.TAG_COMPOUND);
                this.pendingBlockTicks.put(regionName, this.readPendingTicksFromNBT(list, BuiltInRegistries.BLOCK, "Block", Blocks.AIR));
            }
            if (version >= 5) {
                ListTag list = regionTag.getList("PendingFluidTicks", Tag.TAG_COMPOUND);
                this.pendingFluidTicks.put(regionName, this.readPendingTicksFromNBT(list, BuiltInRegistries.FLUID, "Fluid", Fluids.EMPTY));
            }
            if (regionTag.contains("BlockStates", Tag.TAG_LONG_ARRAY)) {
                ListTag palette = regionTag.getList("BlockStatePalette", Tag.TAG_COMPOUND);
                long[] blockStateArr = regionTag.getLongArray("BlockStates");
                BlockPos posEndRel = PositionUtils.getRelativeEndPositionFromAreaSize(regionSize).offset(regionPos);
                BlockPos posMin = PositionUtils.getMinCorner(regionPos, posEndRel);
                BlockPos posMax = PositionUtils.getMaxCorner(regionPos, posEndRel);
                BlockPos size = posMax.subtract(posMin).offset(1, 1, 1);
                LitematicaBlockStateContainer container = LitematicaBlockStateContainer.createFrom(palette, blockStateArr, size);
                if (minecraftDataVersion < MINECRAFT_DATA_VERSION) {
                    ServuxProtocol.LOGGER.warn("Cannot process minecraft data version: {}", minecraftDataVersion);
                }
                this.blockContainers.put(regionName, container);
            }
        }
    }

    private List<EntityInfo> readEntitiesFromNBT(ListTag tagList) {
        List<EntityInfo> entityList = new ArrayList<>();
        final int size = tagList.size();

        for (int i = 0; i < size; ++i) {
            CompoundTag entityData = tagList.getCompound(i);
            Vec3 posVec = NbtUtils.readEntityPositionFromTag(entityData);

            if (posVec != null && !entityData.isEmpty()) {
                entityList.add(new EntityInfo(posVec, entityData));
            }
        }

        return entityList;
    }

    private Map<BlockPos, CompoundTag> readTileEntitiesFromNBT(ListTag tagList) {
        Map<BlockPos, CompoundTag> tileMap = new HashMap<>();
        final int size = tagList.size();

        for (int i = 0; i < size; ++i) {
            CompoundTag tag = tagList.getCompound(i);
            BlockPos pos = NbtUtils.readBlockPos(tag);

            if (pos != null && !tag.isEmpty()) {
                tileMap.put(pos, tag);
            }
        }

        return tileMap;
    }

    private <T> Map<BlockPos, ScheduledTick<T>> readPendingTicksFromNBT(ListTag tagList, Registry<T> registry,
                                                                        String tagName, T emptyValue) {
        Map<BlockPos, ScheduledTick<T>> tickMap = new HashMap<>();
        final int size = tagList.size();
        for (int i = 0; i < size; ++i) {
            CompoundTag tag = tagList.getCompound(i);

            // XXX these were accidentally saved as longs in version 3
            if (!tag.contains("Time", Tag.TAG_ANY_NUMERIC)) {
                continue;
            }
            T target;
            ResourceLocation resourceLocation = ResourceLocation.tryParse(tag.getString(tagName));
            if (resourceLocation == null) {
                continue;
            }
            Optional<Holder.Reference<T>> tReference = registry.get(resourceLocation);
            if (tReference.isEmpty()) {
                continue;
            }
            target = tReference.get().value();
            if (target == emptyValue) {
                continue;
            }
            BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
            TickPriority priority = TickPriority.byValue(tag.getInt("Priority"));
            int scheduledTime = tag.getInt("Time");
            long subTick = tag.getLong("SubTick");
            tickMap.put(pos, new ScheduledTick<>(target, pos, scheduledTime, priority, subTick));
        }

        return tickMap;
    }

    private List<EntityInfo> readEntitiesFromNBT_v1(ListTag tagList) {
        List<EntityInfo> entityList = new ArrayList<>();
        final int size = tagList.size();

        for (int i = 0; i < size; ++i) {
            CompoundTag tag = tagList.getCompound(i);
            Vec3 posVec = NbtUtils.readVec3(tag);
            CompoundTag entityData = tag.getCompound("EntityData");

            if (posVec != null && !entityData.isEmpty()) {
                // Update the correct position to the TileEntity NBT, where it is stored in version 2
                NbtUtils.writeEntityPositionToTag(posVec, entityData);
                entityList.add(new EntityInfo(posVec, entityData));
            }
        }

        return entityList;
    }

    private Map<BlockPos, CompoundTag> readTileEntitiesFromNBT_v1(ListTag tagList) {
        Map<BlockPos, CompoundTag> tileMap = new HashMap<>();
        final int size = tagList.size();

        for (int i = 0; i < size; ++i) {
            CompoundTag tag = tagList.getCompound(i);
            CompoundTag tileNbt = tag.getCompound("TileNBT");

            // Note: This within-schematic relative position is not inside the tile tag!
            BlockPos pos = NbtUtils.readBlockPos(tag);

            if (pos != null && !tileNbt.isEmpty()) {
                // Update the correct position to the entity NBT, where it is stored in version 2
                NbtUtils.writeBlockPosToTag(pos, tileNbt);
                tileMap.put(pos, tileNbt);
            }
        }

        return tileMap;
    }

    public record EntityInfo(Vec3 posVec, CompoundTag nbt) {
        public EntityInfo(Vec3 posVec, CompoundTag nbt) {
            this.posVec = posVec;

            if (nbt.contains("SleepingX", Tag.TAG_INT)) {
                nbt.putInt("SleepingX", Mth.floor(posVec.x));
            }
            if (nbt.contains("SleepingY", Tag.TAG_INT)) {
                nbt.putInt("SleepingY", Mth.floor(posVec.y));
            }
            if (nbt.contains("SleepingZ", Tag.TAG_INT)) {
                nbt.putInt("SleepingZ", Mth.floor(posVec.z));
            }

            this.nbt = nbt;
        }
    }
}