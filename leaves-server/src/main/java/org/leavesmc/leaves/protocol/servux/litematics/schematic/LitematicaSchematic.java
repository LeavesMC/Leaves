package org.leavesmc.leaves.protocol.servux.litematics.schematic;


import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.leavesmc.leaves.command.NoBlockUpdateCommand;
import org.leavesmc.leaves.protocol.servux.litematics.ServuxLitematicsProtocol;
import org.leavesmc.leaves.protocol.servux.litematics.malilib.IntBoundingBox;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.container.ILitematicaBlockStatePalette;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.container.LitematicaBlockStateContainer;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.placement.SchematicPlacement;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.placement.SubRegionPlacement;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.selection.AreaSelection;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.selection.Box;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.utils.DataProviderManager;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.utils.EntityUtils;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.utils.FileType;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.utils.NbtUtils;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.utils.PositionUtils;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.utils.ReplaceBehavior;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.utils.SchematicPlacingUtils;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LitematicaSchematic {
    public static final String FILE_EXTENSION = ".litematic";
    public static final int MINECRAFT_DATA_VERSION_1_12 = 1139; // MC 1.12
    public static final int MINECRAFT_DATA_VERSION = SharedConstants.getProtocolVersion();
    public static final int SCHEMATIC_VERSION = 7;
    // This is basically a "sub-version" for the schematic version,
    // intended to help with possible data fix needs that are discovered.
    public static final int SCHEMATIC_VERSION_SUB = 1; // Bump to one after the sleeping entity position fix

    public final Map<String, LitematicaBlockStateContainer> blockContainers = new HashMap<>();
    public final Map<String, Map<BlockPos, CompoundTag>> tileEntities = new HashMap<>();
    public final Map<String, Map<BlockPos, ScheduledTick<Block>>> pendingBlockTicks = new HashMap<>();
    public final Map<String, Map<BlockPos, ScheduledTick<Fluid>>> pendingFluidTicks = new HashMap<>();
    public final Map<String, List<EntityInfo>> entities = new HashMap<>();
    public final Map<String, BlockPos> subRegionPositions = new HashMap<>();
    public final Map<String, BlockPos> subRegionSizes = new HashMap<>();
    public final SchematicMetadata metadata = new SchematicMetadata();
    private int totalBlocksReadFromLevel;
    @Nullable
    private final Path schematicFile;
    private final FileType schematicType;


    public LitematicaSchematic(CompoundTag nbtCompound) throws CommandSyntaxException {
        this.readFromNBT(nbtCompound);
        this.schematicFile = Path.of("/");
        this.schematicType = FileType.LITEMATICA_SCHEMATIC;
    }

    private LitematicaSchematic(@Nullable Path file) {
        this(file, FileType.LITEMATICA_SCHEMATIC);
    }

    private LitematicaSchematic(@Nullable Path file, FileType schematicType) {
        this.schematicFile = file;
        this.schematicType = schematicType;
    }

    @Nullable
    public Path getFile() {
        return this.schematicFile;
    }

    public Vec3i getTotalSize() {
        return this.metadata.getEnclosingSize();
    }

    public int getTotalBlocksReadFromLevel() {
        return this.totalBlocksReadFromLevel;
    }

    public SchematicMetadata getMetadata() {
        return this.metadata;
    }

    public int getSubRegionCount() {
        return this.blockContainers.size();
    }

    @Nullable
    public BlockPos getSubRegionPosition(String areaName) {
        return this.subRegionPositions.get(areaName);
    }

    public Map<String, BlockPos> getAreaPositions() {
        ImmutableMap.Builder<String, BlockPos> builder = ImmutableMap.builder();

        for (String name : this.subRegionPositions.keySet()) {
            BlockPos pos = this.subRegionPositions.get(name);
            builder.put(name, pos);
        }

        return builder.build();
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

    public Map<String, Box> getAreas() {
        ImmutableMap.Builder<String, Box> builder = ImmutableMap.builder();

        for (String name : this.subRegionPositions.keySet()) {
            BlockPos pos = this.subRegionPositions.get(name);
            BlockPos posEndRel = PositionUtils.getRelativeEndPositionFromAreaSize(this.subRegionSizes.get(name));
            Box box = new Box(pos, pos.offset(posEndRel), name);
            builder.put(name, box);
        }

        return builder.build();
    }

    @Nullable
    public static LitematicaSchematic createFromLevel(Level world, AreaSelection area, SchematicSaveInfo info,
                                                      String author) {
        List<Box> boxes = PositionUtils.getValidBoxes(area);

        if (boxes.isEmpty()) {
            ServuxLitematicsProtocol.LOGGER.warn("createFromLevel: No Selection boxes.");
            return null;
        }

        LitematicaSchematic schematic = new LitematicaSchematic(Path.of("/"));
        long time = System.currentTimeMillis();

        BlockPos origin = area.getEffectiveOrigin();
        schematic.setSubRegionPositions(boxes, origin);
        schematic.setSubRegionSizes(boxes);

        schematic.takeBlocksFromLevel(world, boxes, info);

        if (info.ignoreEntities == false) {
            schematic.takeEntitiesFromLevel(world, boxes, origin);
        }

        schematic.metadata.setAuthor(author);
        schematic.metadata.setName(area.getName());
        schematic.metadata.setTimeCreated(time);
        schematic.metadata.setTimeModified(time);
        schematic.metadata.setRegionCount(boxes.size());
        schematic.metadata.setTotalVolume(PositionUtils.getTotalVolume(boxes));
        schematic.metadata.setEnclosingSize(PositionUtils.getEnclosingAreaSize(boxes));
        schematic.metadata.setTotalBlocks(schematic.totalBlocksReadFromLevel);
        schematic.metadata.setSchematicVersion(SCHEMATIC_VERSION);
        schematic.metadata.setMinecraftDataVersion(MINECRAFT_DATA_VERSION);
        schematic.metadata.setFileType(FileType.LITEMATICA_SCHEMATIC);

        return schematic;
    }

    public boolean placeToLevel(Level world, SchematicPlacement schematicPlacement, boolean notifyNeighbors) {
        return this.placeToLevel(world, schematicPlacement, notifyNeighbors, false);
    }

    public boolean placeToLevel(Level world, SchematicPlacement schematicPlacement, boolean notifyNeighbors, boolean ignoreEntities) {
        NoBlockUpdateCommand.setPreventBlockUpdate(true);

        ImmutableMap<String, SubRegionPlacement> relativePlacements = schematicPlacement.getEnabledRelativeSubRegionPlacements();
        BlockPos origin = schematicPlacement.getOrigin();

        for (String regionName : relativePlacements.keySet()) {
            SubRegionPlacement placement = relativePlacements.get(regionName);

            if (placement.isEnabled()) {
                BlockPos regionPos = placement.getPos();
                BlockPos regionSize = this.subRegionSizes.get(regionName);
                LitematicaBlockStateContainer container = this.blockContainers.get(regionName);
                Map<BlockPos, CompoundTag> tileMap = this.tileEntities.get(regionName);
                List<EntityInfo> entityList = this.entities.get(regionName);
                Map<BlockPos, ScheduledTick<Block>> scheduledBlockTicks = this.pendingBlockTicks.get(regionName);
                Map<BlockPos, ScheduledTick<Fluid>> scheduledFluidTicks = this.pendingFluidTicks.get(regionName);

                if (regionPos != null && regionSize != null && container != null && tileMap != null) {
                    this.placeBlocksToLevel(world, origin, regionPos, regionSize, schematicPlacement, placement, container, tileMap, scheduledBlockTicks, scheduledFluidTicks, notifyNeighbors);
                } else {
                    ServuxLitematicsProtocol.LOGGER.warn("Invalid/missing schematic data in schematic '{}' for sub-region '{}'", this.metadata.getName(), regionName);
                }

                if (ignoreEntities == false && schematicPlacement.ignoreEntities() == false &&
                    placement.ignoreEntities() == false && entityList != null) {
                    this.placeEntitiesToLevel(world, origin, regionPos, regionSize, schematicPlacement, placement, entityList);
                }
            }
        }

        NoBlockUpdateCommand.setPreventBlockUpdate(false);

        return true;
    }

    private boolean placeBlocksToLevel(Level world, BlockPos origin, BlockPos regionPos, BlockPos regionSize,
                                       SchematicPlacement schematicPlacement, SubRegionPlacement placement,
                                       LitematicaBlockStateContainer container, Map<BlockPos, CompoundTag> tileMap,
                                       @Nullable Map<BlockPos, ScheduledTick<Block>> scheduledBlockTicks,
                                       @Nullable Map<BlockPos, ScheduledTick<Fluid>> scheduledFluidTicks, boolean notifyNeighbors) {
        // These are the untransformed relative positions
        BlockPos posEndRelSub = PositionUtils.getRelativeEndPositionFromAreaSize(regionSize);
        BlockPos posEndRel = posEndRelSub.offset(regionPos);
        BlockPos posMinRel = PositionUtils.getMinCorner(regionPos, posEndRel);

        BlockPos regionPosTransformed = PositionUtils.getTransformedBlockPos(regionPos, schematicPlacement.getMirror(), schematicPlacement.getRotation());
        //BlockPos posEndAbs = PositionUtils.getTransformedBlockPos(posEndRelSub, placement.getMirror(), placement.getRotation()).add(regionPosTransformed).add(origin);
        BlockPos regionPosAbs = regionPosTransformed.offset(origin);

        /*
        if (PositionUtils.arePositionsWithinLevel(world, regionPosAbs, posEndAbs) == false)
        {
            return false;
        }
        */

        final int sizeX = Math.abs(regionSize.getX());
        final int sizeY = Math.abs(regionSize.getY());
        final int sizeZ = Math.abs(regionSize.getZ());
        final BlockState barrier = Blocks.BARRIER.defaultBlockState();
        final boolean ignoreInventories = false;
        BlockPos.MutableBlockPos posMutable = new BlockPos.MutableBlockPos();
        ReplaceBehavior replace = ReplaceBehavior.ALL;

        final Rotation rotationCombined = schematicPlacement.getRotation().getRotated(placement.getRotation());
        final Mirror mirrorMain = schematicPlacement.getMirror();
        Mirror mirrorSub = placement.getMirror();

        if (mirrorSub != Mirror.NONE &&
            (schematicPlacement.getRotation() == Rotation.CLOCKWISE_90 ||
                schematicPlacement.getRotation() == Rotation.COUNTERCLOCKWISE_90)) {
            mirrorSub = mirrorSub == Mirror.FRONT_BACK ? Mirror.LEFT_RIGHT : Mirror.FRONT_BACK;
        }

        int bottomY = world.getMinY();
        int topY = world.getMaxSectionY() + 1;
        int tmp = posMinRel.getY() - regionPos.getY() + regionPosTransformed.getY() + origin.getY();
        int startY = 0;
        int endY = sizeY;

        if (tmp < bottomY) {
            startY += (bottomY - tmp);
        }

        tmp = posMinRel.getY() - regionPos.getY() + regionPosTransformed.getY() + origin.getY() + (endY - 1);

        if (tmp > topY) {
            endY -= (tmp - topY);
        }

        for (int y = startY; y < endY; ++y) {
            for (int z = 0; z < sizeZ; ++z) {
                for (int x = 0; x < sizeX; ++x) {
                    BlockState state = container.get(x, y, z);

                    if (state.getBlock() == Blocks.STRUCTURE_VOID) {
                        continue;
                    }

                    posMutable.set(x, y, z);
                    CompoundTag teNBT = tileMap.get(posMutable);

                    posMutable.set(posMinRel.getX() + x - regionPos.getX(),
                        posMinRel.getY() + y - regionPos.getY(),
                        posMinRel.getZ() + z - regionPos.getZ());

                    BlockPos pos = PositionUtils.getTransformedPlacementPosition(posMutable, schematicPlacement, placement);
                    pos = pos.offset(regionPosTransformed).offset(origin);

                    BlockState stateOld = world.getBlockState(pos);

                    if ((replace == ReplaceBehavior.NONE && stateOld.isAir() == false) ||
                        (replace == ReplaceBehavior.WITH_NON_AIR && state.isAir())) {
                        continue;
                    }

                    if (mirrorMain != Mirror.NONE) {
                        state = state.mirror(mirrorMain);
                    }
                    if (mirrorSub != Mirror.NONE) {
                        state = state.mirror(mirrorSub);
                    }
                    if (rotationCombined != Rotation.NONE) {
                        state = state.rotate(rotationCombined);
                    }

                    if (stateOld == state && state.hasBlockEntity() == false) {
                        continue;
                    }

                    BlockEntity teOld = world.getBlockEntity(pos);

                    if (teOld != null) {
                        if (teOld instanceof Container) {
                            ((Container) teOld).clearContent();
                        }

                        world.setBlock(pos, barrier, 0x14);
                    }

                    if (world.setBlock(pos, state, 0x12) && teNBT != null) {
                        BlockEntity te = world.getBlockEntity(pos);

                        if (te != null) {
                            teNBT = teNBT.copy();
                            teNBT.putInt("x", pos.getX());
                            teNBT.putInt("y", pos.getY());
                            teNBT.putInt("z", pos.getZ());

                            if (ignoreInventories) {
                                teNBT.remove("Items");
                            }

                            try {
                                te.loadWithComponents(teNBT, world.registryAccess().freeze());

                                if (ignoreInventories && te instanceof Container) {
                                    ((Container) te).clearContent();
                                }
                            } catch (Exception e) {
                                ServuxLitematicsProtocol.LOGGER.warn("Failed to load TileEntity data for {} @ {}", state, pos);
                            }
                        }
                    }
                }
            }
        }

        /*
        if (notifyNeighbors)
        {
            for (int y = 0; y < sizeY; ++y)
            {
                for (int z = 0; z < sizeZ; ++z)
                {
                    for (int x = 0; x < sizeX; ++x)
                    {
                        posMutable.set( posMinRel.getX() + x - regionPos.getX(),
                                        posMinRel.getY() + y - regionPos.getY(),
                                        posMinRel.getZ() + z - regionPos.getZ());
                        BlockPos pos = PositionUtils.getTransformedPlacementPosition(posMutable, schematicPlacement, placement).add(origin);
                        world.updateNeighbors(pos, world.getBlockState(pos).getBlock());
                    }
                }
            }
        }

        if (world instanceof ServerLevel serverLevel)
        {
            if (scheduledBlockTicks != null && scheduledBlockTicks.isEmpty() == false)
            {
                for (Map.Entry<BlockPos, ScheduledTick<Block>> entry : scheduledBlockTicks.entrySet())
                {
                    BlockPos pos = entry.getKey().add(regionPosAbs);
                    ScheduledTick<Block> tick = entry.getValue();
                    serverLevel.getBlockTickScheduler().scheduleTick(new ScheduledTick<>(tick.type(), pos, (int) tick.triggerTick(), tick.priority(), tick.subTickOrder()));
                }
            }

            if (scheduledFluidTicks != null && scheduledFluidTicks.isEmpty() == false)
            {
                for (Map.Entry<BlockPos, ScheduledTick<Fluid>> entry : scheduledFluidTicks.entrySet())
                {
                    BlockPos pos = entry.getKey().add(regionPosAbs);
                    BlockState state = world.getBlockState(pos);

                    if (state.getFluidState().isEmpty() == false)
                    {
                        ScheduledTick<Fluid> tick = entry.getValue();
                        serverLevel.getFluidTickScheduler().scheduleTick(new ScheduledTick<>(tick.type(), pos, (int) tick.triggerTick(), tick.priority(), tick.subTickOrder()));
                    }
                }
            }
        }
        */

        return true;
    }

    private void placeEntitiesToLevel(Level world, BlockPos origin, BlockPos regionPos, BlockPos regionSize, SchematicPlacement schematicPlacement, SubRegionPlacement placement, List<EntityInfo> entityList) {
        BlockPos regionPosRelTransformed = PositionUtils.getTransformedBlockPos(regionPos, schematicPlacement.getMirror(), schematicPlacement.getRotation());
        final int offX = regionPosRelTransformed.getX() + origin.getX();
        final int offY = regionPosRelTransformed.getY() + origin.getY();
        final int offZ = regionPosRelTransformed.getZ() + origin.getZ();

        final Rotation rotationCombined = schematicPlacement.getRotation().getRotated(placement.getRotation());
        final Mirror mirrorMain = schematicPlacement.getMirror();
        Mirror mirrorSub = placement.getMirror();

        if (mirrorSub != Mirror.NONE &&
            (schematicPlacement.getRotation() == Rotation.CLOCKWISE_90 ||
                schematicPlacement.getRotation() == Rotation.COUNTERCLOCKWISE_90)) {
            mirrorSub = mirrorSub == Mirror.FRONT_BACK ? Mirror.LEFT_RIGHT : Mirror.FRONT_BACK;
        }

        for (EntityInfo info : entityList) {
            Entity entity = EntityUtils.createEntityAndPassengersFromNBT(info.nbt, world);

            if (entity != null) {
                Vec3 pos = info.posVec;
                pos = PositionUtils.getTransformedPosition(pos, schematicPlacement.getMirror(), schematicPlacement.getRotation());
                pos = PositionUtils.getTransformedPosition(pos, placement.getMirror(), placement.getRotation());
                double x = pos.x + offX;
                double y = pos.y + offY;
                double z = pos.z + offZ;

                SchematicPlacingUtils.rotateEntity(entity, x, y, z, rotationCombined, mirrorMain, mirrorSub);
                EntityUtils.createEntityAndPassengersFromNBT(entity, world);
            }
        }
    }

    private void takeEntitiesFromLevel(Level world, List<Box> boxes, BlockPos origin) {
        for (Box box : boxes) {
            AABB bb = PositionUtils.createEnclosingAABB(box.getPos1(), box.getPos2());
            BlockPos regionPosAbs = box.getPos1();
            List<EntityInfo> list = new ArrayList<>();
            List<Entity> entities = world.getEntities((Entity) null, bb, EntityUtils.NOT_PLAYER);

            for (Entity entity : entities) {
                CompoundTag tag = new CompoundTag();

                if (entity.save(tag)) {
                    Vec3 posVec = new Vec3(entity.getX() - regionPosAbs.getX(), entity.getY() - regionPosAbs.getY(), entity.getZ() - regionPosAbs.getZ());
                    NbtUtils.writeEntityPositionToTag(posVec, tag);
                    list.add(new EntityInfo(posVec, tag));
                }
            }

            this.entities.put(box.getName(), list);
        }
    }

    public void takeEntitiesFromLevelWithinChunk(Level world, int chunkX, int chunkZ,
                                                 ImmutableMap<String, IntBoundingBox> volumes, ImmutableMap<String, Box> boxes,
                                                 Set<UUID> existingEntities, BlockPos origin) {
        for (Map.Entry<String, IntBoundingBox> entry : volumes.entrySet()) {
            String regionName = entry.getKey();
            List<EntityInfo> list = this.entities.get(regionName);
            Box box = boxes.get(regionName);

            if (box == null || list == null) {
                continue;
            }

            AABB bb = PositionUtils.createAABBFrom(entry.getValue());
            List<Entity> entities = world.getEntities((Entity) null, bb, EntityUtils.NOT_PLAYER);
            BlockPos regionPosAbs = box.getPos1();

            for (Entity entity : entities) {
                UUID uuid = entity.getUUID();
                /*
                if (entity.posX >= bb.minX && entity.posX < bb.maxX &&
                    entity.posY >= bb.minY && entity.posY < bb.maxY &&
                    entity.posZ >= bb.minZ && entity.posZ < bb.maxZ)
                */
                if (existingEntities.contains(uuid) == false) {
                    CompoundTag tag = new CompoundTag();

                    if (entity.save(tag)) {
                        Vec3 posVec = new Vec3(entity.getX() - regionPosAbs.getX(), entity.getY() - regionPosAbs.getY(), entity.getZ() - regionPosAbs.getZ());

                        // Annoying special case for any hanging/decoration entities, to avoid the console
                        // warning about invalid hanging position when loading the entity from NBT
                        if (entity instanceof HangingEntity decorationEntity) {
                            BlockPos p = decorationEntity.blockPosition();
                            tag.putInt("TileX", p.getX() - regionPosAbs.getX());
                            tag.putInt("TileY", p.getY() - regionPosAbs.getY());
                            tag.putInt("TileZ", p.getZ() - regionPosAbs.getZ());
                        }

                        NbtUtils.writeEntityPositionToTag(posVec, tag);
                        list.add(new EntityInfo(posVec, tag));
                        existingEntities.add(uuid);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void takeBlocksFromLevel(Level world, List<Box> boxes, SchematicSaveInfo info) {
        BlockPos.MutableBlockPos posMutable = new BlockPos.MutableBlockPos(0, 0, 0);

        for (Box box : boxes) {
            BlockPos size = box.getSize();
            final int sizeX = Math.abs(size.getX());
            final int sizeY = Math.abs(size.getY());
            final int sizeZ = Math.abs(size.getZ());
            LitematicaBlockStateContainer container = new LitematicaBlockStateContainer(sizeX, sizeY, sizeZ);
            Map<BlockPos, CompoundTag> tileEntityMap = new HashMap<>();
            Map<BlockPos, ScheduledTick<Block>> blockTickMap = new HashMap<>();
            Map<BlockPos, ScheduledTick<Fluid>> fluidTickMap = new HashMap<>();

            // We want to loop nice & easy from 0 to n here, but the per-sub-region pos1 can be at
            // any corner of the area. Thus we need to offset from the total area origin
            // to the minimum/negative corner (ie. 0,0 in the loop) corner here.
            final BlockPos minCorner = PositionUtils.getMinCorner(box.getPos1(), box.getPos2());
            final int startX = minCorner.getX();
            final int startY = minCorner.getY();
            final int startZ = minCorner.getZ();
            final boolean visibleOnly = info.visibleOnly;
            final boolean includeSupport = info.includeSupportBlocks;

            for (int y = 0; y < sizeY; ++y) {
                for (int z = 0; z < sizeZ; ++z) {
                    for (int x = 0; x < sizeX; ++x) {
                        posMutable.set(x + startX, y + startY, z + startZ);

                        if (visibleOnly &&
                            isExposed(world, posMutable) == false &&
                            (includeSupport == false || isSupport(world, posMutable) == false)) {
                            continue;
                        }

                        BlockState state = world.getBlockState(posMutable);
                        container.set(x, y, z, state);

                        if (state.isAir() == false) {
                            this.totalBlocksReadFromLevel++;
                        }

                        if (state.hasBlockEntity()) {
                            BlockEntity te = world.getBlockEntity(posMutable);

                            if (te != null) {
                                // TODO Add a TileEntity NBT cache from the Chunk packets, to get the original synced data (too)
                                BlockPos pos = new BlockPos(x, y, z);
                                CompoundTag tag = te.saveWithId(world.registryAccess());
                                NbtUtils.writeBlockPosToTag(pos, tag);
                                tileEntityMap.put(pos, tag);
                            }
                        }
                    }
                }
            }

            if (world instanceof ServerLevel serverLevel) {
                IntBoundingBox tickBox = IntBoundingBox.createProper(
                    startX, startY, startZ,
                    startX + sizeX, startY + sizeY, startZ + sizeZ);
                long currentTick = world.getGameTime();


                // MIXIN REQUIRED
                LevelTicks<Block> blockTicks = serverLevel.getBlockTicks();
                LevelTicks<Fluid> fluidTicks = serverLevel.getFluidTicks();
                this.getTicksFromScheduler(blockTicks.servux$getChunkTickSchedulers(), blockTickMap, tickBox, minCorner, currentTick);

                this.getTicksFromScheduler(fluidTicks.servux$getChunkTickSchedulers(), fluidTickMap, tickBox, minCorner, currentTick);
            }

            this.blockContainers.put(box.getName(), container);
            this.tileEntities.put(box.getName(), tileEntityMap);
            this.pendingBlockTicks.put(box.getName(), blockTickMap);
            this.pendingFluidTicks.put(box.getName(), fluidTickMap);
        }
    }

    private <T> void getTicksFromScheduler(Long2ObjectMap<LevelChunkTicks<T>> chunkTickSchedulers,
                                           Map<BlockPos, ScheduledTick<T>> outputMap,
                                           IntBoundingBox box,
                                           BlockPos minCorner,
                                           final long currentTick) {
        int minCX = SectionPos.posToSectionCoord(box.minX);
        int minCZ = SectionPos.posToSectionCoord(box.minZ);
        int maxCX = SectionPos.posToSectionCoord(box.maxX);
        int maxCZ = SectionPos.posToSectionCoord(box.maxZ);

        for (int cx = minCX; cx <= maxCX; ++cx) {
            for (int cz = minCZ; cz <= maxCZ; ++cz) {
                long cp = ChunkPos.asLong(cx, cz);

                LevelChunkTicks<T> chunkTickScheduler = chunkTickSchedulers.get(cp);

                if (chunkTickScheduler != null) {
                    chunkTickScheduler.getAll()
                        .filter((t) -> box.containsPos(t.pos()))
                        .forEach((t) -> this.addRelativeTickToMap(outputMap, t, minCorner, currentTick));
                }
            }
        }
    }

    private <T> void addRelativeTickToMap(Map<BlockPos, ScheduledTick<T>> outputMap, ScheduledTick<T> tick,
                                          BlockPos minCorner, long currentTick) {
        BlockPos pos = tick.pos();
        BlockPos relativePos = new BlockPos(pos.getX() - minCorner.getX(),
            pos.getY() - minCorner.getY(),
            pos.getZ() - minCorner.getZ());

        ScheduledTick<T> newTick = new ScheduledTick<>(tick.type(), relativePos, tick.triggerTick() - currentTick,
            tick.priority(), tick.subTickOrder());

        outputMap.put(relativePos, newTick);
    }

    public static boolean isExposed(Level world, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos posAdj = pos.relative(dir);
            BlockState stateAdj = world.getBlockState(posAdj);


            // sus
            if (!stateAdj.starlight$isConditionallyFullOpaque() ||
                !stateAdj.isFaceSturdy(world, posAdj, dir.getOpposite())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isGravityBlock(BlockState state) {
        return state.is(BlockTags.SAND) ||
            state.is(BlockTags.CONCRETE_POWDER) ||
            state.getBlock() == Blocks.GRAVEL;
    }

    public static boolean isGravityBlock(Level world, BlockPos pos) {
        return isGravityBlock(world.getBlockState(pos));
    }

    public static boolean supportsExposedBlocks(Level world, BlockPos pos) {
        BlockPos posUp = pos.relative(Direction.UP);
        BlockState stateUp = world.getBlockState(posUp);

        while (true) {
            if (needsSupportNonGravity(stateUp)) {
                return true;
            } else if (isGravityBlock(stateUp)) {
                if (isExposed(world, posUp)) {
                    return true;
                }
            } else {
                break;
            }

            posUp = posUp.relative(Direction.UP);

            if (posUp.getY() >= world.getMaxSectionY() + 1) {
                break;
            }

            stateUp = world.getBlockState(posUp);
        }

        return false;
    }

    public static boolean needsSupportNonGravity(BlockState state) {
        Block block = state.getBlock();

        return block == Blocks.REPEATER ||
            block == Blocks.COMPARATOR ||
            block == Blocks.SNOW ||
            block instanceof CarpetBlock; // Moss Carpet is not in the WOOL_CARPETS tag
    }

    public static boolean isSupport(Level world, BlockPos pos) {
        // This only needs to return true for blocks that are needed support for another block,
        // and that other block would possibly block visibility to this block, i.e. its side
        // facing this block position is a full opaque square.
        // Apparently there is no method that indicates blocks that need support...
        // so hard coding a bunch of stuff here it is then :<
        BlockPos posUp = pos.relative(Direction.UP);
        BlockState stateUp = world.getBlockState(posUp);

        if (needsSupportNonGravity(stateUp)) {
            return true;
        }

        return isGravityBlock(stateUp) &&
            (isExposed(world, posUp) || supportsExposedBlocks(world, posUp));
    }

    private void setSubRegionPositions(List<Box> boxes, BlockPos areaOrigin) {
        for (Box box : boxes) {
            this.subRegionPositions.put(box.getName(), box.getPos1().subtract(areaOrigin));
        }
    }

    private void setSubRegionSizes(List<Box> boxes) {
        for (Box box : boxes) {
            this.subRegionSizes.put(box.getName(), box.getSize());
        }
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

    private CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();

        nbt.putInt("MinecraftDataVersion", MINECRAFT_DATA_VERSION);
        nbt.putInt("Version", SCHEMATIC_VERSION);
        nbt.putInt("SubVersion", SCHEMATIC_VERSION_SUB);
        nbt.put("Metadata", this.metadata.writeToNBT());
        nbt.put("Regions", this.writeSubRegionsToNBT());

        return nbt;
    }

    private CompoundTag writeSubRegionsToNBT() {
        CompoundTag wrapper = new CompoundTag();

        if (this.blockContainers.isEmpty() == false) {
            for (String regionName : this.blockContainers.keySet()) {
                LitematicaBlockStateContainer blockContainer = this.blockContainers.get(regionName);
                Map<BlockPos, CompoundTag> tileMap = this.tileEntities.get(regionName);
                List<EntityInfo> entityList = this.entities.get(regionName);
                Map<BlockPos, ScheduledTick<Block>> pendingBlockTicks = this.pendingBlockTicks.get(regionName);
                Map<BlockPos, ScheduledTick<Fluid>> pendingFluidTicks = this.pendingFluidTicks.get(regionName);

                CompoundTag tag = new CompoundTag();

                tag.put("BlockStatePalette", blockContainer.getPalette().writeToNBT());
                tag.put("BlockStates", new LongArrayTag(blockContainer.getBackingLongArray()));
                tag.put("TileEntities", this.writeTileEntitiesToNBT(tileMap));

                if (pendingBlockTicks != null) {
                    tag.put("PendingBlockTicks", this.writePendingTicksToNBT(pendingBlockTicks, BuiltInRegistries.BLOCK, "Block"));
                }

                if (pendingFluidTicks != null) {
                    tag.put("PendingFluidTicks", this.writePendingTicksToNBT(pendingFluidTicks, BuiltInRegistries.FLUID, "Fluid"));
                }

                // The entity list will not exist, if takeEntities is false when creating the schematic
                if (entityList != null) {
                    tag.put("Entities", this.writeEntitiesToNBT(entityList));
                }

                BlockPos pos = this.subRegionPositions.get(regionName);
                tag.put("Position", NbtUtils.createBlockPosTag(pos));

                pos = this.subRegionSizes.get(regionName);
                tag.put("Size", NbtUtils.createBlockPosTag(pos));

                wrapper.put(regionName, tag);
            }
        }

        return wrapper;
    }

    private ListTag writeEntitiesToNBT(List<EntityInfo> entityList) {
        ListTag tagList = new ListTag();

        if (entityList.isEmpty() == false) {
            for (EntityInfo info : entityList) {
                tagList.add(info.nbt);
            }
        }

        return tagList;
    }

    private <T> ListTag writePendingTicksToNBT(Map<BlockPos, ScheduledTick<T>> tickMap, Registry<T> registry, String tagName) {
        ListTag tagList = new ListTag();

        if (tickMap.isEmpty() == false) {
            for (ScheduledTick<T> entry : tickMap.values()) {
                T target = entry.type();
                ResourceLocation id = registry.getKey(target);

                if (id != null) {
                    CompoundTag tag = new CompoundTag();

                    tag.putString(tagName, id.toString());
                    tag.putInt("Priority", entry.priority().getValue());
                    tag.putLong("SubTick", entry.subTickOrder());
                    tag.putInt("Time", (int) entry.triggerTick());
                    tag.putInt("x", entry.pos().getX());
                    tag.putInt("y", entry.pos().getY());
                    tag.putInt("z", entry.pos().getZ());

                    tagList.add(tag);
                }
            }
        }

        return tagList;
    }

    private ListTag writeTileEntitiesToNBT(Map<BlockPos, CompoundTag> tileMap) {
        ListTag tagList = new ListTag();

        if (tileMap.isEmpty() == false) {
            tagList.addAll(tileMap.values());
        }

        return tagList;
    }

    private boolean readFromNBT(CompoundTag nbt) throws CommandSyntaxException {
        this.blockContainers.clear();
        this.tileEntities.clear();
        this.entities.clear();
        this.pendingBlockTicks.clear();
        this.subRegionPositions.clear();
        this.subRegionSizes.clear();
        //this.metadata.clearModifiedSinceSaved();

        if (nbt.contains("Version", Tag.TAG_INT)) {
            final int version = nbt.getInt("Version");
            final int minecraftDataVersion = nbt.contains("MinecraftDataVersion") ? nbt.getInt("MinecraftDataVersion") : SharedConstants.getProtocolVersion();

            if (version >= 1 && version <= SCHEMATIC_VERSION) {
                this.metadata.readFromNBT(nbt.getCompound("Metadata"));
                this.metadata.setSchematicVersion(version);
                this.metadata.setMinecraftDataVersion(minecraftDataVersion);
                this.metadata.setFileType(FileType.LITEMATICA_SCHEMATIC);
                this.readSubRegionsFromNBT(nbt.getCompound("Regions"), version, minecraftDataVersion);

                return true;
            } else {
                error("servux.litematics.error.schematic_load.unsupported_schematic_version");
            }
        } else {
            error("servux.litematics.error.schematic_load.no_schematic_version_information");
        }
        return false;
    }

    private void error(String s, Objects... objects) throws CommandSyntaxException {
        throw new SimpleCommandExceptionType(Component.translatable(s, (Object[]) objects)).create();
    }

    private void error(String s) throws CommandSyntaxException {
        throw new SimpleCommandExceptionType(Component.translatable(s)).create();
    }

    private void readSubRegionsFromNBT(CompoundTag tag, int version, int minecraftDataVersion) {
        for (String regionName : tag.getAllKeys()) {
            if (tag.get(regionName).getId() == Tag.TAG_COMPOUND) {
                CompoundTag regionTag = tag.getCompound(regionName);
                BlockPos regionPos = NbtUtils.readBlockPos(regionTag.getCompound("Position"));
                BlockPos regionSize = NbtUtils.readBlockPos(regionTag.getCompound("Size"));
                Map<BlockPos, CompoundTag> tiles = null;

                if (regionPos != null && regionSize != null) {
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

                    Tag nbtBase = regionTag.get("BlockStates");

                    // There are no convenience methods in NBTTagCompound yet in 1.12, so we'll have to do it the ugly way...
                    if (nbtBase != null && nbtBase.getId() == Tag.TAG_LONG_ARRAY) {
                        ListTag palette = regionTag.getList("BlockStatePalette", Tag.TAG_COMPOUND);
                        long[] blockStateArr = ((LongArrayTag) nbtBase).getAsLongArray();

                        BlockPos posEndRel = PositionUtils.getRelativeEndPositionFromAreaSize(regionSize).offset(regionPos);
                        BlockPos posMin = PositionUtils.getMinCorner(regionPos, posEndRel);
                        BlockPos posMax = PositionUtils.getMaxCorner(regionPos, posEndRel);
                        BlockPos size = posMax.subtract(posMin).offset(1, 1, 1);

                        LitematicaBlockStateContainer container = LitematicaBlockStateContainer.createFrom(palette, blockStateArr, size);

                        if (minecraftDataVersion < MINECRAFT_DATA_VERSION) {
                            this.postProcessContainerIfNeeded(palette, container, tiles);
                        }

                        this.blockContainers.put(regionName, container);
                    }
                }
            }
        }
    }

    public static boolean isSizeValid(@Nullable Vec3i size) {
        return size != null && size.getX() > 0 && size.getY() > 0 && size.getZ() > 0;
    }

    protected boolean readPaletteFromLitematicaFormatTag(ListTag tagList, ILitematicaBlockStatePalette palette) {
        final int size = tagList.size();
        List<BlockState> list = new ArrayList<>(size);
        HolderLookup.RegistryLookup<Block> lookup = DataProviderManager.INSTANCE.getRegistryManager().lookupOrThrow(Registries.BLOCK);

        for (int id = 0; id < size; ++id) {
            CompoundTag tag = tagList.getCompound(id);
            BlockState state = net.minecraft.nbt.NbtUtils.readBlockState(lookup, tag);
            list.add(state);
        }

        return palette.setMapping(list);
    }

    private void postProcessContainerIfNeeded(ListTag palette, LitematicaBlockStateContainer container, @Nullable Map<BlockPos, CompoundTag> tiles) {
        List<BlockState> states = getStatesFromPaletteTag(palette);
    }

    public static List<BlockState> getStatesFromPaletteTag(ListTag palette) {
        List<BlockState> states = new ArrayList<>();
        //RegistryEntryLookup<Block> lookup = Registries.createEntryLookup(Registries.BLOCK);
        HolderLookup.RegistryLookup<Block> lookup = DataProviderManager.INSTANCE.getRegistryManager().lookupOrThrow(Registries.BLOCK);
        final int size = palette.size();

        for (int i = 0; i < size; ++i) {
            CompoundTag tag = palette.getCompound(i);
            BlockState state = net.minecraft.nbt.NbtUtils.readBlockState(lookup, tag);

            if (i > 0 || state != LitematicaBlockStateContainer.AIR_BLOCK_STATE) {
                states.add(state);
            }
        }

        return states;
    }

    private List<EntityInfo> readEntitiesFromNBT(ListTag tagList) {
        List<EntityInfo> entityList = new ArrayList<>();
        final int size = tagList.size();

        for (int i = 0; i < size; ++i) {
            CompoundTag entityData = tagList.getCompound(i);
            Vec3 posVec = NbtUtils.readEntityPositionFromTag(entityData);

            if (posVec != null && entityData.isEmpty() == false) {
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

            if (pos != null && tag.isEmpty() == false) {
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

            if (tag.contains("Time", Tag.TAG_ANY_NUMERIC)) // XXX these were accidentally saved as longs in version 3
            {
                T target = null;

                // Don't crash on invalid ResourceLocation in 1.13+
                try {
                    target = registry.get(ResourceLocation.tryParse(tag.getString(tagName))).get().value();

                    if (target == null || target == emptyValue) {
                        continue;
                    }
                } catch (Exception ignore) {
                }

                if (target != null) {
                    BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
                    TickPriority priority = TickPriority.byValue(tag.getInt("Priority"));
                    // Note: the time is a relative delay at this point
                    int scheduledTime = tag.getInt("Time");
                    long subTick = tag.getLong("SubTick");
                    tickMap.put(pos, new ScheduledTick<>(target, pos, scheduledTime, priority, subTick));
                }
            }
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

            if (posVec != null && entityData.isEmpty() == false) {
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

            if (pos != null && tileNbt.isEmpty() == false) {
                // Update the correct position to the entity NBT, where it is stored in version 2
                NbtUtils.writeBlockPosToTag(pos, tileNbt);
                tileMap.put(pos, tileNbt);
            }
        }

        return tileMap;
    }

    public boolean writeToFile(Path dir, String fileNameIn, boolean override) {
        return this.writeToFile(dir, fileNameIn, override, false);
    }

    public boolean writeToFile(Path dir, String fileNameIn, boolean override, boolean downgrade) {
        String fileName = fileNameIn;

        if (fileName.endsWith(FILE_EXTENSION) == false) {
            fileName = fileName + FILE_EXTENSION;
        }

        Path fileSchematic = dir.resolve(fileName);

        try {
            if (!Files.exists(dir)) {
                Files.createDirectory(dir);
            }

            if (!Files.isDirectory(dir)) {
                //InfoUtils.showGuiOrInGameMessage(MessageType.ERROR, "litematica.error.schematic_write_to_file_failed.directory_creation_failed", dir.toAbsolutePath());
                return false;
            }

            if (override == false && Files.exists(fileSchematic)) {
                //InfoUtils.showGuiOrInGameMessage(MessageType.ERROR, "litematica.error.schematic_write_to_file_failed.exists", fileSchematic.toAbsolutePath());
                return false;
            }

            NbtUtils.writeCompressed(this.writeToNBT(), fileSchematic);

            return true;
        } catch (Exception e) {
            /*
            Litematica.LOGGER.error(StringUtils.translate("litematica.error.schematic_write_to_file_failed.exception", fileSchematic.toAbsolutePath()), e);
             */
        }

        return false;
    }


    public boolean readFromFile() {
        return this.readFromFile(this.schematicType);
    }

    private boolean readFromFile(FileType schematicType) {
        try {
            CompoundTag nbt = readNbtFromFile(this.schematicFile);

            if (nbt != null) {
                if (schematicType == FileType.LITEMATICA_SCHEMATIC) {
                    return this.readFromNBT(nbt);
                }
            }
        } catch (Exception e) {
            //error("servux.litematics.error.schematic_read_from_file_failed.exception", this.schematicFile.toAbsolutePath());
        }

        return false;
    }

    public static CompoundTag readNbtFromFile(Path file) {
        if (file == null) {
            //error("servux.litematics.error.schematic_read_from_file_failed.no_file");
            return null;
        }

        if (Files.exists(file) == false || Files.isReadable(file) == false) {
            //error("servux.litematics.error.schematic_read_from_file_failed.cant_read", file.toAbsolutePath());
            return null;
        }

        return NbtUtils.readNbtFromFileAsPath(file);
    }

    public static Path fileFromDirAndName(Path dir, String fileName, FileType schematicType) {
        if (fileName.endsWith(FILE_EXTENSION) == false && schematicType == FileType.LITEMATICA_SCHEMATIC) {
            fileName = fileName + FILE_EXTENSION;
        }

        return dir.resolve(fileName);
    }

    @Nullable
    public static LitematicaSchematic createFromFile(Path dir, String fileName) {
        return createFromFile(dir, fileName, FileType.LITEMATICA_SCHEMATIC);
    }

    @Nullable
    public static LitematicaSchematic createFromFile(Path dir, String fileName, FileType schematicType) {
        Path file = fileFromDirAndName(dir, fileName, schematicType);
        LitematicaSchematic schematic = new LitematicaSchematic(file, schematicType);

        return schematic.readFromFile(schematicType) ? schematic : null;
    }

    public static class EntityInfo {
        public final Vec3 posVec;
        public final CompoundTag nbt;

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

    public static class SchematicSaveInfo {
        public final boolean visibleOnly;
        public final boolean includeSupportBlocks;
        public final boolean ignoreEntities;
        public final boolean fromSchematicLevel;

        public SchematicSaveInfo(boolean visibleOnly,
                                 boolean ignoreEntities) {
            this(visibleOnly, false, ignoreEntities, false);
        }

        public SchematicSaveInfo(boolean visibleOnly,
                                 boolean includeSupportBlocks,
                                 boolean ignoreEntities,
                                 boolean fromSchematicLevel) {
            this.visibleOnly = visibleOnly;
            this.includeSupportBlocks = includeSupportBlocks;
            this.ignoreEntities = ignoreEntities;
            this.fromSchematicLevel = fromSchematicLevel;
        }
    }
}
