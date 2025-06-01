package org.leavesmc.leaves.protocol.servux.litematics.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.leavesmc.leaves.protocol.servux.ServuxProtocol;
import org.leavesmc.leaves.protocol.servux.litematics.LitematicaSchematic;
import org.leavesmc.leaves.protocol.servux.litematics.container.LitematicaBlockStateContainer;
import org.leavesmc.leaves.protocol.servux.litematics.placement.SchematicPlacement;
import org.leavesmc.leaves.protocol.servux.litematics.placement.SubRegionPlacement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchematicPlacingUtils {
    public static void placeToWorldWithinChunk(
        Level world,
        ChunkPos chunkPos,
        SchematicPlacement schematicPlacement,
        ReplaceBehavior replace,
        boolean notifyNeighbors
    ) {
        LitematicaSchematic schematic = schematicPlacement.getSchematic();
        Set<String> regionsTouchingChunk = schematicPlacement.getRegionsTouchingChunk(chunkPos.x, chunkPos.z);
        BlockPos origin = schematicPlacement.getOrigin();

        for (String regionName : regionsTouchingChunk) {
            LitematicaSchematic.SubRegion subRegion = schematic.getSubRegion(regionName);
            LitematicaBlockStateContainer container = subRegion.blockContainers();

            if (container == null) {
                continue;
            }

            SubRegionPlacement placement = schematicPlacement.getRelativeSubRegionPlacement(regionName);
            if (placement == null) {
                ServuxProtocol.LOGGER.error("receiver a null placement for region: {}", regionName);
                continue;
            }
            if (!placement.enabled()) {
                continue;
            }
            Map<BlockPos, CompoundTag> blockEntityMap = subRegion.tileEntities();
            Map<BlockPos, ScheduledTick<Block>> scheduledBlockTicks = subRegion.pendingBlockTicks();
            Map<BlockPos, ScheduledTick<Fluid>> scheduledFluidTicks = subRegion.pendingFluidTicks();

            if (!placeBlocksWithinChunk(world, chunkPos, regionName, container, blockEntityMap,
                origin, schematicPlacement, placement, scheduledBlockTicks, scheduledFluidTicks, replace, notifyNeighbors)) {
                ServuxProtocol.LOGGER.warn("Invalid/missing schematic data in schematic '{}' for sub-region '{}'", schematic.metadata().name(), regionName);
            }

            List<LitematicaSchematic.EntityInfo> entityList = subRegion.entities();

            if (!schematicPlacement.ignoreEntities() &&
                !placement.ignoreEntities() && entityList != null) {
                placeEntitiesToWorldWithinChunk(world, chunkPos, entityList, origin, schematicPlacement, placement);
            }
        }
    }

    public static boolean placeBlocksWithinChunk(
        Level world, ChunkPos chunkPos, String regionName,
        LitematicaBlockStateContainer container,
        Map<BlockPos, CompoundTag> blockEntityMap,
        BlockPos origin,
        SchematicPlacement schematicPlacement,
        SubRegionPlacement placement,
        @Nullable Map<BlockPos, ScheduledTick<Block>> scheduledBlockTicks,
        @Nullable Map<BlockPos, ScheduledTick<Fluid>> scheduledFluidTicks,
        ReplaceBehavior replace, boolean notifyNeighbors
    ) {
        IntBoundingBox bounds = schematicPlacement.getBoxWithinChunkForRegion(regionName, chunkPos.x, chunkPos.z);
        Vec3i regionSize = schematicPlacement.getSchematic().getSubRegion(regionName).size();

        if (bounds == null || container == null || blockEntityMap == null || regionSize == null) {
            return false;
        }

        BlockPos regionPos = placement.pos();

        // These are the untransformed relative positions
        BlockPos posEndRel = (new BlockPos(PositionUtils.getRelativeEndPositionFromAreaSize(regionSize))).offset(regionPos);
        BlockPos posMinRel = PositionUtils.getMinCorner(regionPos, posEndRel);

        // The transformed sub-region origin position
        BlockPos regionPosTransformed = PositionUtils.getTransformedBlockPos(regionPos, schematicPlacement.getMirror(), schematicPlacement.getRotation());

        // The relative offset of the affected region's corners, to the sub-region's origin corner
        BlockPos boxMinRel = new BlockPos(bounds.minX() - origin.getX() - regionPosTransformed.getX(), 0, bounds.minZ() - origin.getZ() - regionPosTransformed.getZ());
        BlockPos boxMaxRel = new BlockPos(bounds.maxX() - origin.getX() - regionPosTransformed.getX(), 0, bounds.maxZ() - origin.getZ() - regionPosTransformed.getZ());

        // Reverse transform that relative offset, to get the untransformed orientation's offsets
        boxMinRel = PositionUtils.getReverseTransformedBlockPos(boxMinRel, placement.mirror(), placement.rotation());
        boxMaxRel = PositionUtils.getReverseTransformedBlockPos(boxMaxRel, placement.mirror(), placement.rotation());

        boxMinRel = PositionUtils.getReverseTransformedBlockPos(boxMinRel, schematicPlacement.getMirror(), schematicPlacement.getRotation());
        boxMaxRel = PositionUtils.getReverseTransformedBlockPos(boxMaxRel, schematicPlacement.getMirror(), schematicPlacement.getRotation());

        // Get the offset relative to the sub-region's minimum corner, instead of the origin corner (which can be at any corner)
        boxMinRel = boxMinRel.subtract(posMinRel.subtract(regionPos));
        boxMaxRel = boxMaxRel.subtract(posMinRel.subtract(regionPos));

        BlockPos posMin = PositionUtils.getMinCorner(boxMinRel, boxMaxRel);
        BlockPos posMax = PositionUtils.getMaxCorner(boxMinRel, boxMaxRel);

        final int startX = posMin.getX();
        final int startZ = posMin.getZ();
        final int endX = posMax.getX();
        final int endZ = posMax.getZ();
        final int startY = 0;
        final int endY = Math.abs(regionSize.getY()) - 1;
        BlockPos.MutableBlockPos posMutable = new BlockPos.MutableBlockPos();
        if (startX < 0 || startZ < 0 || endX >= container.getSize().getX() || endZ >= container.getSize().getZ()) {
            return false;
        }

        final Rotation rotationCombined = schematicPlacement.getRotation().getRotated(placement.rotation());
        final Mirror mirrorMain = schematicPlacement.getMirror();
        final BlockState barrier = Blocks.BARRIER.defaultBlockState();
        Mirror mirrorSub = placement.mirror();
        final boolean ignoreInventories = false;

        if (mirrorSub != Mirror.NONE &&
            (schematicPlacement.getRotation() == Rotation.CLOCKWISE_90 ||
                schematicPlacement.getRotation() == Rotation.COUNTERCLOCKWISE_90)) {
            mirrorSub = mirrorSub == Mirror.FRONT_BACK ? Mirror.LEFT_RIGHT : Mirror.FRONT_BACK;
        }

        final int posMinRelMinusRegX = posMinRel.getX() - regionPos.getX();
        final int posMinRelMinusRegY = posMinRel.getY() - regionPos.getY();
        final int posMinRelMinusRegZ = posMinRel.getZ() - regionPos.getZ();

        for (int y = startY; y <= endY; ++y) {
            for (int z = startZ; z <= endZ; ++z) {
                for (int x = startX; x <= endX; ++x) {
                    BlockState state = container.get(x, y, z);

                    if (state.getBlock() == Blocks.STRUCTURE_VOID) {
                        continue;
                    }

                    posMutable.set(x, y, z);
                    CompoundTag teNBT = blockEntityMap.get(posMutable);

                    posMutable.set(posMinRelMinusRegX + x,
                        posMinRelMinusRegY + y,
                        posMinRelMinusRegZ + z);

                    BlockPos pos = PositionUtils.getTransformedPlacementPosition(posMutable, schematicPlacement, placement);
                    pos = pos.offset(regionPosTransformed).offset(origin);

                    BlockState stateOld = world.getBlockState(pos);

                    if ((replace == ReplaceBehavior.NONE && !stateOld.isAir()) ||
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

                    BlockEntity te = world.getBlockEntity(pos);

                    if (te != null) {
                        if (te instanceof Container) {
                            ((Container) te).clearContent();
                        }

                        world.setBlock(pos, barrier, 2 | 16 | (notifyNeighbors ? 0 : 512));
                    }

                    if (world.setBlock(pos, state, 2 | 16 | (notifyNeighbors ? 0 : 512)) && teNBT != null) {
                        te = world.getBlockEntity(pos);

                        if (te == null) {
                            continue;
                        }
                        teNBT = teNBT.copy();
                        NbtUtils.writeBlockPosToTag(pos, teNBT);

                        try {
                            te.loadWithComponents(teNBT, MinecraftServer.getServer().registryAccess());
                        } catch (Exception e) {
                            ServuxProtocol.LOGGER.warn("Failed to load BlockEntity data for {} @ {}", state, pos);
                        }
                    }
                }
            }
        }
        ServerLevel serverWorld = (ServerLevel) world;
        IntBoundingBox box = new IntBoundingBox(startX, startY, startZ, endX, endY, endZ);

        if (scheduledBlockTicks != null && !scheduledBlockTicks.isEmpty()) {
            LevelTicks<Block> scheduler = serverWorld.getBlockTicks();

            for (Map.Entry<BlockPos, ScheduledTick<Block>> entry : scheduledBlockTicks.entrySet()) {
                BlockPos pos = entry.getKey();

                if (!box.containsPos(pos)) {
                    continue;
                }
                posMutable.set(posMinRelMinusRegX + pos.getX(),
                    posMinRelMinusRegY + pos.getY(),
                    posMinRelMinusRegZ + pos.getZ());

                pos = PositionUtils.getTransformedPlacementPosition(posMutable, schematicPlacement, placement);
                pos = pos.offset(regionPosTransformed).offset(origin);
                ScheduledTick<Block> tick = entry.getValue();

                if (world.getBlockState(pos).getBlock() == tick.type()) {
                    scheduler.schedule(new ScheduledTick<>(tick.type(), pos, tick.triggerTick(), tick.priority(), tick.subTickOrder()));
                }
            }
        }

        if (scheduledFluidTicks != null && !scheduledFluidTicks.isEmpty()) {
            LevelTicks<Fluid> scheduler = serverWorld.getFluidTicks();

            for (Map.Entry<BlockPos, ScheduledTick<Fluid>> entry : scheduledFluidTicks.entrySet()) {
                BlockPos pos = entry.getKey();

                if (!box.containsPos(pos)) {
                    continue;
                }
                posMutable.set(posMinRelMinusRegX + pos.getX(),
                    posMinRelMinusRegY + pos.getY(),
                    posMinRelMinusRegZ + pos.getZ());

                pos = PositionUtils.getTransformedPlacementPosition(posMutable, schematicPlacement, placement);
                pos = pos.offset(regionPosTransformed).offset(origin);
                ScheduledTick<Fluid> tick = entry.getValue();

                if (world.getBlockState(pos).getFluidState().getType() == tick.type()) {
                    scheduler.schedule(new ScheduledTick<>(tick.type(), pos, tick.triggerTick(), tick.priority(), tick.subTickOrder()));
                }
            }
        }

        if (!notifyNeighbors) {
            return true;
        }
        for (int y = startY; y <= endY; ++y) {
            for (int z = startZ; z <= endZ; ++z) {
                for (int x = startX; x <= endX; ++x) {
                    posMutable.set(posMinRelMinusRegX + x,
                        posMinRelMinusRegY + y,
                        posMinRelMinusRegZ + z);
                    BlockPos pos = PositionUtils.getTransformedPlacementPosition(posMutable, schematicPlacement, placement);
                    pos = pos.offset(regionPosTransformed).offset(origin);
                    world.updateNeighborsAt(pos, world.getBlockState(pos).getBlock());
                }
            }
        }

        return true;
    }

    public static void placeEntitiesToWorldWithinChunk(
        Level world, ChunkPos chunkPos,
        List<LitematicaSchematic.EntityInfo> entityList,
        BlockPos origin,
        SchematicPlacement schematicPlacement,
        SubRegionPlacement placement
    ) {
        BlockPos regionPos = placement.pos();

        if (entityList == null) {
            return;
        }

        BlockPos regionPosRelTransformed = PositionUtils.getTransformedBlockPos(regionPos, schematicPlacement.getMirror(), schematicPlacement.getRotation());
        final int offX = regionPosRelTransformed.getX() + origin.getX();
        final int offY = regionPosRelTransformed.getY() + origin.getY();
        final int offZ = regionPosRelTransformed.getZ() + origin.getZ();
        final double minX = (chunkPos.x << 4);
        final double minZ = (chunkPos.z << 4);
        final double maxX = (chunkPos.x << 4) + 16;
        final double maxZ = (chunkPos.z << 4) + 16;

        final Rotation rotationCombined = schematicPlacement.getRotation().getRotated(placement.rotation());
        final Mirror mirrorMain = schematicPlacement.getMirror();
        Mirror mirrorSub = placement.mirror();

        if (mirrorSub != Mirror.NONE &&
            (schematicPlacement.getRotation() == Rotation.CLOCKWISE_90 ||
                schematicPlacement.getRotation() == Rotation.COUNTERCLOCKWISE_90)) {
            mirrorSub = mirrorSub == Mirror.FRONT_BACK ? Mirror.LEFT_RIGHT : Mirror.FRONT_BACK;
        }

        for (LitematicaSchematic.EntityInfo info : entityList) {
            Vec3 pos = info.posVec();
            pos = PositionUtils.getTransformedPosition(pos, schematicPlacement.getMirror(), schematicPlacement.getRotation());
            pos = PositionUtils.getTransformedPosition(pos, placement.mirror(), placement.rotation());
            double x = pos.x + offX;
            double y = pos.y + offY;
            double z = pos.z + offZ;
            float[] origRot = new float[2];

            if (!(x >= minX && x < maxX && z >= minZ && z < maxZ)) {
                continue;
            }
            CompoundTag tag = info.nbt().copy();
            String id = tag.getStringOr("id", "");

            // Avoid warning about invalid hanging position.
            // Note that this position isn't technically correct, but it only needs to be within 16 blocks
            // of the entity position to avoid the warning.
            if (id.equals("minecraft:glow_item_frame") ||
                id.equals("minecraft:item_frame") ||
                id.equals("minecraft:leash_knot") ||
                id.equals("minecraft:painting")) {
                Vec3 p = NbtUtils.readEntityPositionFromTag(tag);

                if (p == null) {
                    p = new Vec3(x, y, z);
                    NbtUtils.writeEntityPositionToTag(p, tag);
                }

                tag.store("block_pos", BlockPos.CODEC, new BlockPos((int) p.x, (int) p.y, (int) p.z));
            }

            ListTag rotation = tag.getListOrEmpty("Rotation");
            origRot[0] = rotation.getFloatOr(0, 0F);
            origRot[1] = rotation.getFloatOr(1, 0F);

            Entity entity = EntityUtils.createEntityAndPassengersFromNBT(tag, world);

            if (entity == null) {
                continue;
            }
            rotateEntity(entity, x, y, z, rotationCombined, mirrorMain, mirrorSub);

            // Update the sleeping position to the current position
            if (entity instanceof LivingEntity living && living.isSleeping()) {
                living.setSleepingPos(BlockPos.containing(x, y, z));
            }

            // Hack fix to fix the painting position offsets.
            // The vanilla code will end up moving the position by one in two of the orientations,
            // because it sets the hanging position to the given position (floored)
            // and then it offsets the position from the hanging position
            // by 0.5 or 1.0 blocks depending on the painting size.
            if (entity instanceof Painting paintingEntity) {
                Direction right = PositionUtils.rotateYCounterclockwise(paintingEntity.getDirection());

                if ((paintingEntity.getVariant().value().width() % 2) == 0 &&
                    right.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                    x -= 1.0 * right.getStepX();
                    z -= 1.0 * right.getStepZ();
                }

                if ((paintingEntity.getVariant().value().height() % 2) == 0) {
                    y -= 1.0;
                }

                entity.teleportTo(x, y, z);
            }
            if (entity instanceof ItemFrame frameEntity) {
                if (frameEntity.getYRot() != origRot[0] && (frameEntity.getXRot() == 90.0F || frameEntity.getXRot() == -90.0F)) {
                    // Fix Yaw only if Pitch is +/- 90.0F (Floor, Ceiling mounted)
                    frameEntity.setYRot(origRot[0]);
                }
            }

            EntityUtils.spawnEntityAndPassengersInWorld(entity, world);

            if (entity instanceof Display) {
                entity.tick(); // Required to set the full data for rendering
            }
        }
    }

    public static void rotateEntity(
        Entity entity, double x, double y, double z,
        Rotation rotationCombined, Mirror mirrorMain, Mirror mirrorSub
    ) {
        float rotationYaw = entity.getYRot();

        if (mirrorMain != Mirror.NONE) {
            rotationYaw = entity.mirror(mirrorMain);
        }
        if (mirrorSub != Mirror.NONE) {
            rotationYaw = entity.mirror(mirrorSub);
        }
        if (rotationCombined != Rotation.NONE) {
            rotationYaw += entity.getYRot() - entity.rotate(rotationCombined);
        }

        entity.snapTo(x, y, z, rotationYaw, entity.getXRot());
        EntityUtils.setEntityRotations(entity, rotationYaw, entity.getXRot());
    }
}