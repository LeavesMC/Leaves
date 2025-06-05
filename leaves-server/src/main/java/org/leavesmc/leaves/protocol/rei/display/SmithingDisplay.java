package org.leavesmc.leaves.protocol.rei.display;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.item.equipment.trim.TrimPattern;
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

    public enum SmithingRecipeType {
        TRIM,
        TRANSFORM,
        ;

        public static final Codec<SmithingRecipeType> CODEC = Codec.STRING.xmap(SmithingRecipeType::valueOf, SmithingRecipeType::name);
        public static final IntFunction<SmithingRecipeType> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, SmithingRecipeType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
    }

    public static class Trimming extends SmithingDisplay {
        private static final StreamCodec<RegistryFriendlyByteBuf, SmithingDisplay.Trimming> CODEC = StreamCodec.composite(
            EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
            SmithingDisplay.Trimming::getInputEntries,
            EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
            SmithingDisplay.Trimming::getOutputEntries,
            ByteBufCodecs.optional(SmithingRecipeType.STREAM_CODEC),
            SmithingDisplay.Trimming::getOptionalType,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
            SmithingDisplay.Trimming::getOptionalLocation,
            TrimPattern.STREAM_CODEC,
            SmithingDisplay.Trimming::pattern,
            SmithingDisplay.Trimming::of
        );

        private static final ResourceLocation SERIALIZER_ID = ResourceLocation.tryBuild("minecraft", "default/smithing/trimming");

        private final Holder<TrimPattern> pattern;

        public Trimming(
            @NotNull List<EntryIngredient> inputs,
            @NotNull List<EntryIngredient> outputs,
            @NotNull SmithingRecipeType type,
            @NotNull ResourceLocation location,
            @NotNull Holder<TrimPattern> pattern
        ) {
            super(inputs, outputs, type, location);
            this.pattern = pattern;
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public static Trimming of(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<SmithingRecipeType> type, Optional<ResourceLocation> location, Holder<TrimPattern> pattern) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResourceLocation getSerializerId() {
            return SERIALIZER_ID;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ? extends Display> streamCodec() {
            return CODEC;
        }

        public Holder<TrimPattern> pattern() {
            return pattern;
        }
    }
}
