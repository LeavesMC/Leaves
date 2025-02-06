package org.leavesmc.leaves.protocol.jade.accessor;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapEncoder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.Level;

public class DataAccessor extends CompoundTag {

    private final Level level;
    private DynamicOps<Tag> ops;

    public DataAccessor(Level level) {
        this.level = level;
    }

    public DynamicOps<Tag> nbtOps() {
        if (ops == null) {
            ops = RegistryOps.create(NbtOps.INSTANCE, level.registryAccess());
        }

        return ops;
    }

    public <D> void writeMapData(MapEncoder<D> codec, D value) {
        Tag tag = codec.encode(value, nbtOps(), nbtOps().mapBuilder()).build(new CompoundTag()).getOrThrow();
        this.merge((CompoundTag) tag);
    }
}
