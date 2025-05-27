package org.leavesmc.leaves.protocol.servux.litematics;

import com.google.common.collect.ImmutableMap;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.leavesmc.leaves.protocol.servux.ServuxProtocol;
import org.leavesmc.leaves.protocol.servux.litematics.container.LitematicaBlockStateContainer;
import org.leavesmc.leaves.protocol.servux.litematics.utils.FileType;
import org.leavesmc.leaves.protocol.servux.litematics.utils.NbtUtils;
import org.leavesmc.leaves.protocol.servux.litematics.utils.PositionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record LitematicaSchematic(Map<String, SubRegion> subRegions, SchematicMetadata metadata) {

    public static final int MINECRAFT_DATA_VERSION = SharedConstants.getProtocolVersion();
    public static final int SCHEMATIC_VERSION = 7;

    @NotNull
    @Contract("_ -> new")
    public static LitematicaSchematic readFromNBT(@NotNull CompoundTag nbt) {
        if (nbt.contains("Version")) {
            final int version = nbt.getIntOr("Version", -1);
            final int minecraftDataVersion = nbt.contains("MinecraftDataVersion") ? nbt.getInt("MinecraftDataVersion").orElseThrow() : SharedConstants.getProtocolVersion();

            if (version >= 1 && version <= SCHEMATIC_VERSION) {
                SchematicMetadata metadata = SchematicMetadata.readFromNBT(nbt.getCompoundOrEmpty("Metadata"), version, minecraftDataVersion, FileType.LITEMATICA_SCHEMATIC);
                Map<String, SubRegion> subRegions = readSubRegionsFromNBT(nbt.getCompoundOrEmpty("Regions"), version, minecraftDataVersion);
                return new LitematicaSchematic(subRegions, metadata);
            } else {
                throw new RuntimeException("Unsupported or future schematic version");
            }
        } else {
            throw new RuntimeException("The schematic doesn't have version information, and can't be safely loaded!");
        }
    }

    @NotNull
    private static Map<String, SubRegion> readSubRegionsFromNBT(@NotNull CompoundTag tag, int version, int minecraftDataVersion) {
        Map<String, SubRegion> subRegions = new HashMap<>();
        for (String regionName : tag.keySet()) {
            Tag region = tag.get(regionName);
            if (region == null || region.getId() != Tag.TAG_COMPOUND) {
                throw new RuntimeException("Unknown region: " + regionName);
            }
            subRegions.put(regionName, SubRegion.readFromNBT(tag.getCompoundOrEmpty(regionName), version, minecraftDataVersion));
        }
        return subRegions;
    }

    @NotNull
    @Unmodifiable
    public Map<String, BlockPos> getAreaSizes() {
        ImmutableMap.Builder<String, BlockPos> builder = ImmutableMap.builderWithExpectedSize(subRegions.size());
        for (Map.Entry<String, SubRegion> entry : subRegions.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().size());
        }
        return builder.build();
    }

    @NotNull
    public SubRegion getSubRegion(String name) {
        return Objects.requireNonNull(subRegions.get(name));
    }

    public record SubRegion(
        LitematicaBlockStateContainer blockContainers,
        Map<BlockPos, CompoundTag> tileEntities,
        Map<BlockPos, ScheduledTick<Block>> pendingBlockTicks,
        Map<BlockPos, ScheduledTick<Fluid>> pendingFluidTicks,
        List<EntityInfo> entities,
        BlockPos position,
        BlockPos size
    ) {
        @NotNull
        @Contract("_, _, _ -> new")
        public static SubRegion readFromNBT(@NotNull CompoundTag regionTag, int version, int minecraftDataVersion) {
            BlockPos position = NbtUtils.readBlockPos(regionTag.getCompoundOrEmpty("Position"));
            BlockPos size = NbtUtils.readBlockPos(regionTag.getCompoundOrEmpty("Size"));
            if (position == null || size == null) {
                throw new IllegalArgumentException("Invalid region");
            }

            Map<BlockPos, CompoundTag> tileEntities;
            List<EntityInfo> entities;
            if (version >= 2) {
                tileEntities = readTileEntitiesFromNBT(regionTag.getListOrEmpty("TileEntities"));
                entities = readEntitiesFromNBT(regionTag.getListOrEmpty("Entities"));
            } else {
                tileEntities = readTileEntitiesFromNBT_v1(regionTag.getListOrEmpty("TileEntities"));
                entities = readEntitiesFromNBT_v1(regionTag.getListOrEmpty("Entities"));
            }

            Map<BlockPos, ScheduledTick<Block>> pendingBlockTicks = null;
            if (version >= 3) {
                pendingBlockTicks = readPendingTicksFromNBT(regionTag.getListOrEmpty("PendingBlockTicks"), BuiltInRegistries.BLOCK, "Block", Blocks.AIR);
            }

            Map<BlockPos, ScheduledTick<Fluid>> pendingFluidTicks = null;
            if (version >= 5) {
                pendingFluidTicks = readPendingTicksFromNBT(regionTag.getListOrEmpty("PendingFluidTicks"), BuiltInRegistries.FLUID, "Fluid", Fluids.EMPTY);
            }

            LitematicaBlockStateContainer blockContainers = null;
            Tag blockState = regionTag.get("BlockStates");
            if (blockState != null && blockState.getId() == Tag.TAG_LONG_ARRAY) {
                ListTag palette = regionTag.getListOrEmpty("BlockStatePalette");
                long[] blockStateArr = ((LongArrayTag) blockState).getAsLongArray();
                BlockPos posEndRel = PositionUtils.getRelativeEndPositionFromAreaSize(size).offset(position);
                BlockPos posMin = PositionUtils.getMinCorner(position, posEndRel);
                BlockPos posMax = PositionUtils.getMaxCorner(position, posEndRel);
                BlockPos containerSize = posMax.subtract(posMin).offset(1, 1, 1);
                blockContainers = LitematicaBlockStateContainer.createFrom(palette, blockStateArr, containerSize);
                if (minecraftDataVersion < MINECRAFT_DATA_VERSION) {
                    ServuxProtocol.LOGGER.warn("Cannot process minecraft data version: {}", minecraftDataVersion);
                }
            }

            return new SubRegion(blockContainers, tileEntities, pendingBlockTicks, pendingFluidTicks, entities, position, size);
        }

        private static List<EntityInfo> readEntitiesFromNBT(ListTag tagList) {
            List<EntityInfo> entityList = new ArrayList<>();
            final int size = tagList.size();

            for (int i = 0; i < size; ++i) {
                CompoundTag entityData = tagList.getCompoundOrEmpty(i);
                Vec3 posVec = NbtUtils.readEntityPositionFromTag(entityData);

                if (posVec != null && !entityData.isEmpty()) {
                    entityList.add(new EntityInfo(posVec, entityData));
                }
            }

            return entityList;
        }

        private static Map<BlockPos, CompoundTag> readTileEntitiesFromNBT(ListTag tagList) {
            Map<BlockPos, CompoundTag> tileMap = new HashMap<>();
            final int size = tagList.size();

            for (int i = 0; i < size; ++i) {
                CompoundTag tag = tagList.getCompoundOrEmpty(i);
                BlockPos pos = NbtUtils.readBlockPos(tag);

                if (pos != null && !tag.isEmpty()) {
                    tileMap.put(pos, tag);
                }
            }

            return tileMap;
        }

        private static <T> Map<BlockPos, ScheduledTick<T>> readPendingTicksFromNBT(ListTag tagList, Registry<T> registry, String tagName, T emptyValue) {
            Map<BlockPos, ScheduledTick<T>> tickMap = new HashMap<>();
            final int size = tagList.size();
            for (int i = 0; i < size; ++i) {
                CompoundTag tag = tagList.getCompoundOrEmpty(i);

                // XXX these were accidentally saved as longs in version 3
                if (!tag.contains("Time")) {
                    continue;
                }
                T target;
                ResourceLocation resourceLocation = ResourceLocation.tryParse(tag.getStringOr(tagName, ""));
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
                BlockPos pos = new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0));
                TickPriority priority = TickPriority.byValue(tag.getIntOr("Priority", 0));
                int scheduledTime = tag.getIntOr("Time", 0);
                long subTick = tag.getLongOr("SubTick", 0L);
                tickMap.put(pos, new ScheduledTick<>(target, pos, scheduledTime, priority, subTick));
            }

            return tickMap;
        }

        private static List<EntityInfo> readEntitiesFromNBT_v1(ListTag tagList) {
            List<EntityInfo> entityList = new ArrayList<>();
            final int size = tagList.size();

            for (int i = 0; i < size; ++i) {
                CompoundTag tag = tagList.getCompoundOrEmpty(i);
                Vec3 posVec = NbtUtils.readVec3(tag);
                CompoundTag entityData = tag.getCompoundOrEmpty("EntityData");

                if (posVec != null && !entityData.isEmpty()) {
                    // Update the correct position to the TileEntity NBT, where it is stored in version 2
                    NbtUtils.writeEntityPositionToTag(posVec, entityData);
                    entityList.add(new EntityInfo(posVec, entityData));
                }
            }

            return entityList;
        }

        private static Map<BlockPos, CompoundTag> readTileEntitiesFromNBT_v1(ListTag tagList) {
            Map<BlockPos, CompoundTag> tileMap = new HashMap<>();
            final int size = tagList.size();

            for (int i = 0; i < size; ++i) {
                CompoundTag tag = tagList.getCompoundOrEmpty(i);
                CompoundTag tileNbt = tag.getCompoundOrEmpty("TileNBT");

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
    }

    public record EntityInfo(Vec3 posVec, CompoundTag nbt) {
        public EntityInfo(Vec3 posVec, CompoundTag nbt) {
            this.posVec = posVec;

            if (nbt.contains("SleepingX")) {
                nbt.putInt("SleepingX", Mth.floor(posVec.x));
            }
            if (nbt.contains("SleepingY")) {
                nbt.putInt("SleepingY", Mth.floor(posVec.y));
            }
            if (nbt.contains("SleepingZ")) {
                nbt.putInt("SleepingZ", Mth.floor(posVec.z));
            }

            this.nbt = nbt;
        }
    }
}