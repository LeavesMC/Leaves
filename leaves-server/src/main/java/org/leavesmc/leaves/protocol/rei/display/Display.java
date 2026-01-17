package org.leavesmc.leaves.protocol.rei.display;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.ProvidesTrimMaterial;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.MapCloningRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.crafting.TippedArrowRecipe;
import net.minecraft.world.item.crafting.TransmuteRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.rei.ingredient.EntryIngredient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A display to be used alongside Roughly Enough Items.
 * <p>
 * see me.shedaniel.rei.api.common.display.Display
 */
public abstract class Display {

    protected Identifier id;

    protected List<EntryIngredient> inputs;

    protected List<EntryIngredient> outputs;

    public Display(@NotNull List<EntryIngredient> inputs,
                   @NotNull List<EntryIngredient> outputs,
                   @NotNull Identifier id) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.id = id;
    }

    @SuppressWarnings("unchecked")
    public static StreamCodec<RegistryFriendlyByteBuf, Display> dispatchCodec() {
        return new StreamCodec<>() {
            @NotNull
            @Override
            public Display decode(@NotNull RegistryFriendlyByteBuf buffer) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull Display display) {
                new FriendlyByteBuf(buffer).writeIdentifier(display.getSerializerId());
                ((StreamCodec<RegistryFriendlyByteBuf, Display>) display.streamCodec()).encode(buffer, display);
            }
        };
    }

    public static Collection<Display> ofTransmuteRecipe(@NotNull RecipeHolder<TransmuteRecipe> recipeHolder) {
        TransmuteRecipe recipe = recipeHolder.value();
        List<RecipeDisplay> displays = recipe.display();
        List<Display> displayList = new ArrayList<>();
        if (!displays.isEmpty()) {
            RecipeDisplay recipeDisplay = displays.getFirst();
            if (recipeDisplay instanceof ShapelessCraftingRecipeDisplay shapelessRecipeDisplay) {
                displayList.add(new ShapelessDisplay(shapelessRecipeDisplay, recipeHolder.id().identifier()));
            } else if (recipeDisplay instanceof ShapedCraftingRecipeDisplay shapelessRecipe) {
                displayList.add(new ShapedDisplay(shapelessRecipe, recipeHolder.id().identifier()));
            }
        }
        return displayList;
    }

    /**
     * see me.shedaniel.rei.plugin.client.categories.crafting.filler.TippedArrowRecipeFiller#apply
     */
    @NotNull
    public static Collection<Display> ofTippedArrowRecipe(@NotNull RecipeHolder<TippedArrowRecipe> recipeHolder) {
        EntryIngredient arrowIngredient = EntryIngredient.of(Items.ARROW);
        Set<Identifier> registeredPotions = new HashSet<>();
        List<Display> displays = new ArrayList<>();
        MinecraftServer.getServer().registryAccess().lookup(Registries.POTION).stream()
            .flatMap(Registry::listElements)
            .map(reference -> PotionContents.createItemStack(Items.LINGERING_POTION, reference))
            .forEach(itemStack -> {
                PotionContents potion = itemStack.get(DataComponents.POTION_CONTENTS);
                if (potion == null || potion.potion().isEmpty()) {
                    return;
                }
                if (potion.potion().get().unwrapKey().isPresent() && registeredPotions.add(potion.potion().get().unwrapKey().get().identifier())) {
                    List<EntryIngredient> input = new ArrayList<>();
                    for (int i = 0; i < 4; i++) {
                        input.add(arrowIngredient);
                    }
                    input.add(EntryIngredient.of(itemStack));
                    for (int i = 0; i < 4; i++) {
                        input.add(arrowIngredient);
                    }
                    ItemStack outputStack = new ItemStack(Items.TIPPED_ARROW, 8);
                    outputStack.set(DataComponents.POTION_CONTENTS, potion);
                    displays.add(new CustomDisplay(input, List.of(EntryIngredient.of(outputStack)), recipeHolder.id().identifier()));
                }
            });
        return displays;
    }

    /**
     * see me.shedaniel.rei.plugin.client.categories.crafting.filler.TippedArrowRecipeFiller#apply
     */
    @NotNull
    public static Collection<Display> ofFireworkRocketRecipe(@NotNull RecipeHolder<FireworkRocketRecipe> recipeHolder) {
        EntryIngredient[] inputs = new EntryIngredient[4];
        inputs[0] = EntryIngredient.of(Items.GUNPOWDER);
        inputs[1] = EntryIngredient.of(Items.PAPER);
        inputs[2] = EntryIngredient.of(new ItemStack(Items.AIR), new ItemStack(Items.GUNPOWDER), new ItemStack(Items.GUNPOWDER));
        inputs[3] = EntryIngredient.of(new ItemStack(Items.AIR), new ItemStack(Items.AIR), new ItemStack(Items.GUNPOWDER));
        ItemStack[] outputs = new ItemStack[3];
        for (int i = 0; i < 3; i++) {
            outputs[i] = new ItemStack(Items.FIREWORK_ROCKET, 3);
            outputs[i].set(DataComponents.FIREWORKS, new Fireworks(i + 1, List.of()));
        }
        return Collections.singleton(new ShapelessDisplay(List.of(inputs), List.of(EntryIngredient.of(outputs)), recipeHolder.id().identifier()));
    }

    /**
     * see me.shedaniel.rei.plugin.client.categories.crafting.filler.MapCloningRecipeFiller#apply
     */
    @NotNull
    public static Collection<Display> ofMapCloningRecipe(@NotNull RecipeHolder<MapCloningRecipe> recipeHolder) {
        return Collections.singleton(
            new ShapelessDisplay(
                List.of(EntryIngredient.of(Items.FILLED_MAP), EntryIngredient.of(Items.MAP)),
                List.of(EntryIngredient.of(new ItemStack(Items.FILLED_MAP, 2))),
                recipeHolder.id().identifier())
        );
    }

    /**
     * see me.shedaniel.rei.plugin.common.displays.DefaultSmithingDisplay#ofTransforming
     */
    @NotNull
    public static SmithingDisplay ofTransforming(RecipeHolder<SmithingTransformRecipe> recipeHolder) {
        return new SmithingDisplay(
            List.of(
                recipeHolder.value().templateIngredient().map(EntryIngredient::ofIngredient).orElse(EntryIngredient.empty()),
                EntryIngredient.ofIngredient(recipeHolder.value().baseIngredient()),
                recipeHolder.value().additionIngredient().map(EntryIngredient::ofIngredient).orElse(EntryIngredient.empty())
            ),
            List.of(ofSlotDisplay(recipeHolder.value().getResult())),
            SmithingDisplay.SmithingRecipeType.TRANSFORM,
            recipeHolder.id().identifier()
        );
    }

    /**
     * see me.shedaniel.rei.plugin.common.displays.DefaultSmithingDisplay#fromTrimming
     */
    @SuppressWarnings("deprecation")
    @NotNull
    public static Collection<Display> ofSmithingTrimRecipe(@NotNull RecipeHolder<SmithingTrimRecipe> recipeHolder) {
        RegistryAccess registryAccess = MinecraftServer.getServer().registryAccess();
        SmithingTrimRecipe recipe = recipeHolder.value();
        List<Display> displays = new ArrayList<>();
        for (Holder<Item> additionStack : (Iterable<Holder<Item>>) recipe.additionIngredient().map(Ingredient::items).orElse(Stream.of())::iterator) {
            Holder<TrimMaterial> trimMaterial = getMaterialFromIngredient(registryAccess, additionStack).orElse(null);
            if (trimMaterial == null) {
                continue;
            }

            EntryIngredient baseIngredient = EntryIngredient.ofIngredient(recipe.baseIngredient());
            displays.add(new SmithingDisplay.Trimming(List.of(
                recipe.templateIngredient().map(EntryIngredient::ofIngredient).orElse(EntryIngredient.empty()),
                baseIngredient,
                EntryIngredient.ofItemHolder(additionStack)
            ), List.of(baseIngredient), SmithingDisplay.SmithingRecipeType.TRIM, recipeHolder.id().identifier(), recipe.pattern()));

        }
        return displays;
    }

    private static Optional<Holder<TrimMaterial>> getMaterialFromIngredient(HolderLookup.Provider provider, Holder<Item> item) {
        ProvidesTrimMaterial providesTrimMaterial = new ItemStack(item).get(DataComponents.PROVIDES_TRIM_MATERIAL);
        return providesTrimMaterial != null ? providesTrimMaterial.unwrap(provider) : Optional.empty();
    }

    public static EntryIngredient ofSlotDisplay(SlotDisplay slot) {
        return switch (slot) {
            case SlotDisplay.Empty ignored -> EntryIngredient.empty();
            case SlotDisplay.ItemSlotDisplay s -> EntryIngredient.of(s.item().value());
            case SlotDisplay.ItemStackSlotDisplay s -> EntryIngredient.of(s.stack());
            case SlotDisplay.TagSlotDisplay s -> ofItemTag(s.tag());
            case SlotDisplay.Composite s -> {
                ArrayList<ItemStack> list = new ArrayList<>();
                for (SlotDisplay slotDisplay : s.contents()) {
                    ofSlotDisplay(slotDisplay).stream().forEach(list::add);
                }
                yield EntryIngredient.of(list.toArray(new ItemStack[0]));
            }
            // REI Bad idea
            case SlotDisplay.AnyFuel ignored -> EntryIngredient.empty();
            default -> {
                RegistryAccess access = MinecraftServer.getServer().registryAccess();
                try {
                    List<ItemStack> stacks = slot.resolveForStacks(new ContextMap.Builder()
                        .withParameter(SlotDisplayContext.REGISTRIES, access)
                        .create(SlotDisplayContext.CONTEXT));
                    yield EntryIngredient.of(stacks.toArray(new ItemStack[0]));
                } catch (Exception e) {
                    MinecraftServer.LOGGER.warn("Failed to resolve slot display: {}", slot, e);
                    yield EntryIngredient.empty();
                }
            }
        };
    }

    public static List<EntryIngredient> ofSlotDisplays(Collection<SlotDisplay> slots) {
        if (slots instanceof Collection<?> collection && collection.isEmpty()) {
            return Collections.emptyList();
        }
        ImmutableList.Builder<EntryIngredient> ingredients = ImmutableList.builder();
        for (SlotDisplay slot : slots) {
            ingredients.add(ofSlotDisplay(slot));
        }
        return ingredients.build();
    }

    public static <T extends ItemLike> EntryIngredient ofItemTag(TagKey<T> tagKey) {
        HolderGetter<T> getter = MinecraftServer.getServer().registryAccess().lookupOrThrow(tagKey.registry());
        HolderSet.Named<T> holders = getter.get(tagKey).orElse(null);
        if (holders == null) {
            return EntryIngredient.empty();
        }

        int size = holders.size();
        if (size == 0) {
            return EntryIngredient.empty();
        }
        if (size == 1) {
            return EntryIngredient.of(new ItemStack(holders.get(0).value()));
        }

        List<ItemStack> stackList = new ArrayList<>();
        for (Holder<T> t : holders) {
            ItemStack stack = new ItemStack(t.value());
            if (!stack.isEmpty()) {
                stackList.add(stack);
            }
        }
        return EntryIngredient.of(stackList.toArray(new ItemStack[0]));
    }

    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    public Identifier getDisplayLocation() {
        return id;
    }

    public Optional<Identifier> getOptionalLocation() {
        return Optional.ofNullable(id);
    }

    public abstract Identifier getSerializerId();

    public abstract StreamCodec<RegistryFriendlyByteBuf, ? extends Display> streamCodec();
}
