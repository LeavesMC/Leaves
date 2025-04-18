package org.leavesmc.leaves.protocol.servux.litematics.utils;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class EntityUtils {

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
        }
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

    public static void spawnEntityAndPassengersInWorld(Entity entity, Level world) {
        ImmutableList<Entity> passengers = entity.passengers;
        if (world.addFreshEntity(entity) && !passengers.isEmpty()) {
            for (Entity passenger : passengers) {
                passenger.absMoveTo(
                    entity.getX(),
                    entity.getY() + entity.getPassengerRidingPosition(passenger).y(),
                    entity.getZ(),
                    passenger.getYRot(), passenger.getXRot()
                );
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
}