package org.leavesmc.leaves.protocol.servux.litematics.schematic.utils;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.placement.SchematicPlacement;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.placement.SubRegionPlacement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class EntityUtils {
    public static final Predicate<Entity> NOT_PLAYER = entity -> (entity instanceof Player) == false;

    public static boolean isCreativeMode(Player player) {
        return player.isCreative();
    }

    public static Direction getHorizontalLookingDirection(Entity entity) {
        return Direction.fromYRot(entity.getYRot());
    }

    public static Direction getVerticalLookingDirection(Entity entity) {
        return entity.getXRot() > 0 ? Direction.DOWN : Direction.UP;
    }

    public static Direction getClosestLookingDirection(Entity entity) {
        if (entity.getXRot() > 60.0f) {
            return Direction.DOWN;
        } else if (-entity.getXRot() > 60.0f) {
            return Direction.UP;
        }

        return getHorizontalLookingDirection(entity);
    }

    @Nullable
    public static <T extends Entity> T findEntityByUUID(List<T> list, UUID uuid) {
        if (uuid == null) {
            return null;
        }

        for (T entity : list) {
            if (entity.getUUID().equals(uuid)) {
                return entity;
            }
        }

        return null;
    }

    public static String getEntityId(Entity entity) {
        EntityType<?> entitytype = entity.getType();
        ResourceLocation resourcelocation = EntityType.getKey(entitytype);
        return resourcelocation.toString();
    }

    @Nullable
    private static Entity createEntityFromNBTSingle(CompoundTag nbt, Level world) {
        try {
            Optional<Entity> optional = EntityType.create(nbt, world, EntitySpawnReason.LOAD);

            if (optional.isPresent()) {
                Entity entity = optional.get();
                entity.setUUID(UUID.randomUUID());
                return entity;
            }
        } catch (Exception ignore) {
        }

        return null;
    }

    /**
     * Note: This does NOT spawn any of the entities in the world!
     *
     * @param nbt   ()
     * @param world ()
     * @return ()
     */
    @Nullable
    public static Entity createEntityAndPassengersFromNBT(CompoundTag nbt, Level world) {
        Entity entity = createEntityFromNBTSingle(nbt, world);

        if (entity == null) {
            return null;
        } else {
            if (nbt.contains("Passengers", Tag.TAG_LIST)) {
                ListTag taglist = nbt.getList("Passengers", Tag.TAG_LIST);

                for (int i = 0; i < taglist.size(); ++i) {
                    Entity passenger = createEntityAndPassengersFromNBT(taglist.getCompound(i), world);

                    if (passenger != null) {
                        passenger.startRiding(entity, true);
                    }
                }
            }

            return entity;
        }
    }

    public static void spawnEntityAndPassengersInWorld(Entity entity, Level world) {
        ImmutableList<Entity> passengers = entity.passengers;
        if (world.addFreshEntity(entity) && !passengers.isEmpty()) {
            for (Entity passenger : passengers) {
                passenger.absMoveTo(
                    entity.getX(),
                    entity.getY() + entity.getPassengerRidingPosition(passenger).y(),
                    entity.getZ(),
                    passenger.getYRot(), passenger.getXRot());
                setEntityRotations(passenger, passenger.getYRot(), passenger.getXRot());
                spawnEntityAndPassengersInWorld(passenger, world);
            }
        }
    }

    public static void setEntityRotations(Entity entity, float yaw, float pitch) {
        entity.setYRot(yaw);
        entity.yRotO = yaw;

        entity.setXRot(pitch);
        entity.xRotO = pitch;

        if (entity instanceof LivingEntity livingBase) {
            livingBase.yHeadRot = yaw;
            livingBase.yBodyRot = yaw;
            livingBase.yHeadRotO = yaw;
            livingBase.yBodyRotO = yaw;
        }
    }

    public static List<Entity> getEntitiesWithinSubRegion(Level world, BlockPos origin, BlockPos regionPos, BlockPos regionSize,
                                                          SchematicPlacement schematicPlacement, SubRegionPlacement placement) {
        // These are the untransformed relative positions
        BlockPos regionPosRelTransformed = PositionUtils.getTransformedBlockPos(regionPos, schematicPlacement.getMirror(), schematicPlacement.getRotation());
        BlockPos posEndAbs = PositionUtils.getTransformedPlacementPosition(regionSize.offset(-1, -1, -1), schematicPlacement, placement).offset(regionPosRelTransformed).offset(origin);
        BlockPos regionPosAbs = regionPosRelTransformed.offset(origin);
        AABB bb = PositionUtils.createEnclosingAABB(regionPosAbs, posEndAbs);

        return world.getEntities((Entity) null, bb, EntityUtils.NOT_PLAYER);
    }
}