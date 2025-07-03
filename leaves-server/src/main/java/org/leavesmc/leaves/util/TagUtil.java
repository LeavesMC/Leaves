package org.leavesmc.leaves.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jetbrains.annotations.Nullable;

public class TagUtil {

    public static CompoundTag saveEntity(@Nullable Entity entity) {
        if (entity == null) {
            return new CompoundTag();
        }
        TagValueOutput output = TagFactory.output();
        entity.save(output);
        return output.buildResult();
    }

    public static boolean saveEntity(Entity entity, CompoundTag tag) {
        if (entity == null) {
            return false;
        }
        TagValueOutput output = TagFactory.output(tag);
        return entity.save(output);
    }

    public static CompoundTag saveTileWithId(@Nullable BlockEntity entity) {
        if (entity == null) {
            return new CompoundTag();
        }
        TagValueOutput output = TagFactory.output();
        entity.saveWithId(output);
        return output.buildResult();
    }

    public static boolean saveEntityAsPassenger(@Nullable Entity entity, CompoundTag tag) {
        if (entity == null) {
            return false;
        }
        TagValueOutput output = TagFactory.output(tag);
        return entity.saveAsPassenger(output);
    }

}
