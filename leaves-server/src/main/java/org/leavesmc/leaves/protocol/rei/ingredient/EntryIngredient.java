package org.leavesmc.leaves.protocol.rei.ingredient;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class EntryIngredient {
    private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Item>> ITEM_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM);
    private static final ResourceLocation ITEM_ID = ResourceLocation.withDefaultNamespace("item");
    public static final StreamCodec<RegistryFriendlyByteBuf, EntryIngredient> CODEC = new StreamCodec<>() {
        @NotNull
        @Override
        public EntryIngredient decode(@NotNull RegistryFriendlyByteBuf buffer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull EntryIngredient value) {
            ByteBufCodecs.writeCount(buffer, value.size(), Integer.MAX_VALUE);
            value.stream().forEach(itemStack -> {
                buffer.writeResourceLocation(ITEM_ID);
                if (itemStack.isEmpty()) {
                    buffer.writeVarInt(0);
                } else {
                    buffer.writeVarInt(itemStack.getCount());
                    ITEM_STREAM_CODEC.encode(buffer, itemStack.getItemHolder());
                    DataComponentPatch.STREAM_CODEC.encode(buffer, itemStack.components.asPatch());
                }
            });
        }
    };
    private static final EntryIngredient EMPTY = new EntryIngredient(new ItemStack[0]);
    @NotNull
    private final ItemStack[] array;

    private EntryIngredient(@NotNull ItemStack[] array) {
        this.array = Objects.requireNonNull(array);
    }

    public static EntryIngredient empty() {
        return EMPTY;
    }

    public static EntryIngredient ofItemHolder(@NotNull Holder<? extends ItemLike> item) {
        return EntryIngredient.of(item.value());
    }

    public static EntryIngredient of(@NotNull ItemLike item) {
        return EntryIngredient.of(new ItemStack(item));
    }

    public static EntryIngredient of(@NotNull ItemStack itemStack) {
        return new EntryIngredient(new ItemStack[]{itemStack});
    }

    public static EntryIngredient of(@NotNull ItemStack... itemStacks) {
        return new EntryIngredient(Arrays.copyOf(itemStacks, itemStacks.length));
    }

    @SuppressWarnings("deprecation")
    public static EntryIngredient ofIngredient(Ingredient ingredient) {
        if (ingredient.isEmpty()) {
            return EntryIngredient.empty();
        }
        ItemStack[] itemStacks = ingredient.items()
            .map(itemHolder -> itemHolder.value().getDefaultInstance())
            .toArray(ItemStack[]::new);
        return EntryIngredient.of(itemStacks);
    }

    public Stream<ItemStack> stream() {
        return Arrays.stream(array);
    }

    public boolean isEmpty() {
        return array.length == 0;
    }

    public ItemStack get(int index) {
        return array[index].copy();
    }

    public int size() {
        return array.length;
    }
}
