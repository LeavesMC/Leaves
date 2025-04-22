package org.leavesmc.leaves.protocol.rei.display;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.rei.ingredient.EntryIngredient;

import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

public class SmithingDisplay extends Display {
    private static final StreamCodec<RegistryFriendlyByteBuf, SmithingDisplay> CODEC = StreamCodec.composite(
        EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
        SmithingDisplay::getInputEntries,
        EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
        SmithingDisplay::getOutputEntries,
        ByteBufCodecs.optional(SmithingRecipeType.STREAM_CODEC),
        SmithingDisplay::getOptionalType,
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
        SmithingDisplay::getOptionalLocation,
        SmithingDisplay::of
    );

    private static final ResourceLocation SERIALIZER_ID = ResourceLocation.tryBuild("minecraft", "default/smithing");

    private final SmithingRecipeType type;

    public Optional<SmithingRecipeType> getOptionalType() {
        return Optional.of(type);
    }

    @Override
    public ResourceLocation getSerializerId() {
        return SERIALIZER_ID;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends Display> streamCodec() {
        return CODEC;
    }

    public SmithingDisplay(
        @NotNull List<EntryIngredient> inputs,
        @NotNull List<EntryIngredient> outputs,
        @NotNull SmithingRecipeType type,
        @NotNull ResourceLocation location
    ) {
        super(inputs, outputs, location);
        this.type = type;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static SmithingDisplay of(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<SmithingRecipeType> type, Optional<ResourceLocation> location) {
        throw new UnsupportedOperationException();
    }

    public enum SmithingRecipeType {
        TRIM,
        TRANSFORM,
        ;

        public static final Codec<SmithingRecipeType> CODEC = Codec.STRING.xmap(SmithingRecipeType::valueOf, SmithingRecipeType::name);
        public static final IntFunction<SmithingRecipeType> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, SmithingRecipeType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
    }
}
