package org.leavesmc.leaves.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
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

    public static CompoundTag saveEntityWithoutId(Entity entity) {
        if (entity == null) {
            return new CompoundTag();
        }
        TagValueOutput output = TagFactory.output();
        entity.saveWithoutId(output);
        return output.buildResult();
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

    public static void loadEntity(Entity entity, CompoundTag tag) {
        if (entity == null) {
            return;
        }
        TagValueInput input = TagFactory.input(tag);
        entity.load(input);
    }

    public static void loadTileWithComponents(BlockEntity entity, CompoundTag tag) {
        if (entity == null) {
            return;
        }
        TagValueInput input = TagFactory.input(tag);
        entity.loadWithComponents(input);
    }

}
