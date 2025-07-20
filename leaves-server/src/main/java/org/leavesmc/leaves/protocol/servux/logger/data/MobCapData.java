package org.leavesmc.leaves.protocol.servux.logger.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.MobCategory;

import java.util.List;

public record MobCapData(List<Cap> data, long worldTick) {
    public static final int CAP_COUNT = MobCategory.values().length;

    private static MobCapData of(int count, List<Cap> data, long worldTick) {
        return new MobCapData(data, worldTick);
    }

    public static final Codec<MobCapData> CODEC = RecordCodecBuilder.create(
        (inst) -> inst.group(
            PrimitiveCodec.INT.fieldOf("cap_count").forGetter($ -> CAP_COUNT),
            Codec.list(Cap.CODEC).fieldOf("cap_data").forGetter(MobCapData::data),
            PrimitiveCodec.LONG.fieldOf("WorldTick").forGetter(MobCapData::worldTick)
        ).apply(inst, MobCapData::of)
    );

    public record Cap(int current, int cap) {
        public static Codec<Cap> CODEC = RecordCodecBuilder.create(
            (inst) -> inst.group(
                PrimitiveCodec.INT.fieldOf("current").forGetter(Cap::current),
                PrimitiveCodec.INT.fieldOf("cap").forGetter(Cap::cap)
            ).apply(inst, Cap::new)
        );
    }
}