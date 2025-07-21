package org.leavesmc.leaves.protocol.servux.logger.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TickData(double mspt, double tps, long sprintTicks, boolean frozen, boolean sprinting, boolean stepping) {
    public static Codec<TickData> CODEC = RecordCodecBuilder.create(
        (inst) -> inst.group(
            PrimitiveCodec.DOUBLE.fieldOf("mspt").forGetter(TickData::mspt),
            PrimitiveCodec.DOUBLE.fieldOf("tps").forGetter(TickData::tps),
            PrimitiveCodec.LONG.fieldOf("sprintTicks").forGetter(TickData::sprintTicks),
            PrimitiveCodec.BOOL.fieldOf("frozen").forGetter(TickData::frozen),
            PrimitiveCodec.BOOL.fieldOf("sprinting").forGetter(TickData::sprinting),
            PrimitiveCodec.BOOL.fieldOf("stepping").forGetter(TickData::stepping)
        ).apply(inst, TickData::new)
    );
}