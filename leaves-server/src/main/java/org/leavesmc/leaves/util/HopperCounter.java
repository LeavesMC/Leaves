package org.leavesmc.leaves.util;

import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

// Powered by fabric-carpet(https://github.com/gnembon/fabric-carpet)

public class HopperCounter {

    private static boolean enabled = false;
    private static final Map<DyeColor, HopperCounter> COUNTERS;

    static {
        EnumMap<DyeColor, HopperCounter> counterMap = new EnumMap<>(DyeColor.class);
        for (DyeColor color : DyeColor.values()) {
            counterMap.put(color, new HopperCounter(color));
        }
        COUNTERS = Collections.unmodifiableMap(counterMap);
    }

    public final DyeColor color;
    private final TextComponent coloredName;
    private final Object2LongMap<Item> counter = new Object2LongLinkedOpenHashMap<>();
    private long startTick;
    private long startMillis;

    private HopperCounter(DyeColor color) {
        this.startTick = -1;
        this.color = color;
        this.coloredName = Component.text(color.getName(), TextColor.color(color.getTextColor()));
    }

    public void add(MinecraftServer server, ItemStack stack) {
        if (startTick < 0) {
            startTick = server.overworld().getGameTime();
            startMillis = System.currentTimeMillis();
        }
        Item item = stack.getItem();
        counter.put(item, counter.getLong(item) + stack.getCount());
    }

    public void reset(MinecraftServer server) {
        counter.clear();
        startTick = server.overworld().getGameTime();
        startMillis = System.currentTimeMillis();
    }

    public static void resetAll(MinecraftServer server, boolean fresh) {
        for (HopperCounter counter : COUNTERS.values()) {
            counter.reset(server);
            if (fresh) {
                counter.startTick = -1;
            }
        }
    }

    public List<Component> format(MinecraftServer server, boolean realTime) {
        long ticks = Math.max(realTime ? (System.currentTimeMillis() - startMillis) / 50 : server.overworld().getGameTime() - startTick, -1);

        if (startTick < 0 || ticks == -1) {
            return Collections.singletonList(Component.text().append(coloredName, Component.text(" hasn't started counting yet")).build());
        }

        long total = getTotalItems();
        if (total <= 0) {
            return Collections.singletonList(Component.text()
                .append(Component.text("No items for "), coloredName)
                .append(Component.text(" yet ("), Component.text(String.format("%.2f ", ticks / (20.0 * 60.0)), Style.style(TextDecoration.BOLD)))
                .append(Component.text("min"), Component.text(realTime ? " - real time" : ""), Component.text(")"))
                .build());
        }

        List<Component> items = new ArrayList<>();
        items.add(Component.text()
            .append(Component.text("Items for "), coloredName, Component.text(" "))
            .append(Component.text("("), Component.text(String.format("%.2f ", ticks * 1.0 / (20 * 60)), Style.style(TextDecoration.BOLD)))
            .append(Component.text("min"), Component.text(realTime ? " - real time" : ""), Component.text("), "))
            .append(Component.text("total: "), Component.text(total, Style.style(TextDecoration.BOLD)), Component.text(", "))
            .append(Component.text("("), Component.text(String.format("%.1f", total * 1.0 * (20 * 60 * 60) / ticks), Style.style(TextDecoration.BOLD)))
            .append(Component.text("/h):"))
            .build());

        items.addAll(counter.object2LongEntrySet().stream().sorted((e, f) -> Long.compare(f.getLongValue(), e.getLongValue())).map(entry -> {
            Item item = entry.getKey();
            Component name = Component.translatable(item.getDescriptionId());
            TextColor textColor = guessColor(server, item);

            if (textColor != null) {
                name = name.style(name.style().merge(Style.style(textColor)));
            } else {
                name = name.style(name.style().merge(Style.style(TextDecoration.ITALIC)));
            }

            long count = entry.getLongValue();
            return Component.text()
                .append(Component.text("- ", NamedTextColor.GRAY))
                .append(name)
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(count, Style.style(TextDecoration.BOLD)), Component.text(", ", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.1f", count * (20.0 * 60.0 * 60.0) / ticks), Style.style(TextDecoration.BOLD)))
                .append(Component.text("/h"))
                .build();
        }).toList());
        return items;
    }

    private static final Map<Item, Block> DEFAULTS = Map.<Item, Block>ofEntries(
        entry(Items.DANDELION, Blocks.YELLOW_WOOL),
        entry(Items.POPPY, Blocks.RED_WOOL),
        entry(Items.BLUE_ORCHID, Blocks.LIGHT_BLUE_WOOL),
        entry(Items.ALLIUM, Blocks.MAGENTA_WOOL),
        entry(Items.AZURE_BLUET, Blocks.SNOW_BLOCK),
        entry(Items.RED_TULIP, Blocks.RED_WOOL),
        entry(Items.ORANGE_TULIP, Blocks.ORANGE_WOOL),
        entry(Items.WHITE_TULIP, Blocks.SNOW_BLOCK),
        entry(Items.PINK_TULIP, Blocks.PINK_WOOL),
        entry(Items.OXEYE_DAISY, Blocks.SNOW_BLOCK),
        entry(Items.CORNFLOWER, Blocks.BLUE_WOOL),
        entry(Items.WITHER_ROSE, Blocks.BLACK_WOOL),
        entry(Items.LILY_OF_THE_VALLEY, Blocks.WHITE_WOOL),
        entry(Items.BROWN_MUSHROOM, Blocks.BROWN_MUSHROOM_BLOCK),
        entry(Items.RED_MUSHROOM, Blocks.RED_MUSHROOM_BLOCK),
        entry(Items.STICK, Blocks.OAK_PLANKS),
        entry(Items.GOLD_INGOT, Blocks.GOLD_BLOCK),
        entry(Items.IRON_INGOT, Blocks.IRON_BLOCK),
        entry(Items.DIAMOND, Blocks.DIAMOND_BLOCK),
        entry(Items.NETHERITE_INGOT, Blocks.NETHERITE_BLOCK),
        entry(Items.SUNFLOWER, Blocks.YELLOW_WOOL),
        entry(Items.LILAC, Blocks.MAGENTA_WOOL),
        entry(Items.ROSE_BUSH, Blocks.RED_WOOL),
        entry(Items.PEONY, Blocks.PINK_WOOL),
        entry(Items.CARROT, Blocks.ORANGE_WOOL),
        entry(Items.APPLE, Blocks.RED_WOOL),
        entry(Items.WHEAT, Blocks.HAY_BLOCK),
        entry(Items.PORKCHOP, Blocks.PINK_WOOL),
        entry(Items.RABBIT, Blocks.PINK_WOOL),
        entry(Items.CHICKEN, Blocks.WHITE_TERRACOTTA),
        entry(Items.BEEF, Blocks.NETHERRACK),
        entry(Items.ENCHANTED_GOLDEN_APPLE, Blocks.GOLD_BLOCK),
        entry(Items.COD, Blocks.WHITE_TERRACOTTA),
        entry(Items.SALMON, Blocks.ACACIA_PLANKS),
        entry(Items.ROTTEN_FLESH, Blocks.BROWN_WOOL),
        entry(Items.PUFFERFISH, Blocks.YELLOW_TERRACOTTA),
        entry(Items.TROPICAL_FISH, Blocks.ORANGE_WOOL),
        entry(Items.POTATO, Blocks.WHITE_TERRACOTTA),
        entry(Items.MUTTON, Blocks.RED_WOOL),
        entry(Items.BEETROOT, Blocks.NETHERRACK),
        entry(Items.MELON_SLICE, Blocks.MELON),
        entry(Items.POISONOUS_POTATO, Blocks.SLIME_BLOCK),
        entry(Items.SPIDER_EYE, Blocks.NETHERRACK),
        entry(Items.GUNPOWDER, Blocks.GRAY_WOOL),
        entry(Items.TURTLE_SCUTE, Blocks.LIME_WOOL),
        entry(Items.ARMADILLO_SCUTE, Blocks.ANCIENT_DEBRIS),
        entry(Items.FEATHER, Blocks.WHITE_WOOL),
        entry(Items.FLINT, Blocks.BLACK_WOOL),
        entry(Items.LEATHER, Blocks.SPRUCE_PLANKS),
        entry(Items.GLOWSTONE_DUST, Blocks.GLOWSTONE),
        entry(Items.PAPER, Blocks.WHITE_WOOL),
        entry(Items.BRICK, Blocks.BRICKS),
        entry(Items.INK_SAC, Blocks.BLACK_WOOL),
        entry(Items.SNOWBALL, Blocks.SNOW_BLOCK),
        entry(Items.WATER_BUCKET, Blocks.WATER),
        entry(Items.LAVA_BUCKET, Blocks.LAVA),
        entry(Items.MILK_BUCKET, Blocks.WHITE_WOOL),
        entry(Items.CLAY_BALL, Blocks.CLAY),
        entry(Items.COCOA_BEANS, Blocks.COCOA),
        entry(Items.BONE, Blocks.BONE_BLOCK),
        entry(Items.COD_BUCKET, Blocks.BROWN_TERRACOTTA),
        entry(Items.PUFFERFISH_BUCKET, Blocks.YELLOW_TERRACOTTA),
        entry(Items.SALMON_BUCKET, Blocks.PINK_TERRACOTTA),
        entry(Items.TROPICAL_FISH_BUCKET, Blocks.ORANGE_TERRACOTTA),
        entry(Items.SUGAR, Blocks.WHITE_WOOL),
        entry(Items.BLAZE_POWDER, Blocks.GOLD_BLOCK),
        entry(Items.ENDER_PEARL, Blocks.WARPED_PLANKS),
        entry(Items.NETHER_STAR, Blocks.DIAMOND_BLOCK),
        entry(Items.PRISMARINE_CRYSTALS, Blocks.SEA_LANTERN),
        entry(Items.PRISMARINE_SHARD, Blocks.PRISMARINE),
        entry(Items.RABBIT_HIDE, Blocks.OAK_PLANKS),
        entry(Items.CHORUS_FRUIT, Blocks.PURPUR_BLOCK),
        entry(Items.SHULKER_SHELL, Blocks.SHULKER_BOX),
        entry(Items.NAUTILUS_SHELL, Blocks.BONE_BLOCK),
        entry(Items.HEART_OF_THE_SEA, Blocks.CONDUIT),
        entry(Items.HONEYCOMB, Blocks.HONEYCOMB_BLOCK),
        entry(Items.NAME_TAG, Blocks.BONE_BLOCK),
        entry(Items.TOTEM_OF_UNDYING, Blocks.YELLOW_TERRACOTTA),
        entry(Items.TRIDENT, Blocks.PRISMARINE),
        entry(Items.GHAST_TEAR, Blocks.WHITE_WOOL),
        entry(Items.PHANTOM_MEMBRANE, Blocks.BONE_BLOCK),
        entry(Items.EGG, Blocks.BONE_BLOCK),
        entry(Items.COPPER_INGOT, Blocks.COPPER_BLOCK),
        entry(Items.AMETHYST_SHARD, Blocks.AMETHYST_BLOCK)
    );

    @Nullable
    public static TextColor guessColor(@NotNull MinecraftServer server, Item item) {
        RegistryAccess registryAccess = server.registryAccess();
        TextColor direct = fromItem(item, registryAccess);
        if (direct != null) {
            return direct;
        }

        ResourceLocation id = registryAccess.lookupOrThrow(Registries.ITEM).getKey(item);
        if (id == null) {
            return null;
        }


        for (Recipe<?> recipe : getRecipesForOutput(server.getRecipeManager(), id, server.overworld())) {
            for (Ingredient ingredient : recipe.placementInfo().ingredients()) {
                Optional<Holder<Item>> match = ingredient.items().filter(stack -> fromItem(stack.value(), registryAccess) != null).findFirst();
                if (match.isPresent()) {
                    return fromItem(match.get().value(), registryAccess);
                }
            }
        }
        return null;
    }

    @NotNull
    public static List<Recipe<?>> getRecipesForOutput(@NotNull RecipeManager recipeManager, ResourceLocation id, Level level) {
        List<Recipe<?>> results = new ArrayList<>();
        ContextMap context = SlotDisplayContext.fromLevel(level);
        recipeManager.getRecipes().forEach(recipe -> {
            for (RecipeDisplay recipeDisplay : recipe.value().display()) {
                recipeDisplay.result().resolveForStacks(context).forEach(stack -> {
                    if (BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()).unwrapKey().map(ResourceKey::location).orElseThrow(IllegalStateException::new).equals(id)) {
                        results.add(recipe.value());
                    }
                });
            }
        });
        return results;
    }

    @Nullable
    public static TextColor fromItem(Item item, RegistryAccess registryAccess) {
        if (DEFAULTS.containsKey(item)) {
            return TextColor.color(appropriateColor(DEFAULTS.get(item).defaultMapColor().col));
        }
        if (item instanceof DyeItem dye) {
            return TextColor.color(appropriateColor(dye.getDyeColor().getMapColor().col));
        }

        Block block = null;
        final Registry<Item> itemRegistry = registryAccess.lookupOrThrow(Registries.ITEM);
        final Registry<Block> blockRegistry = registryAccess.lookupOrThrow(Registries.BLOCK);
        ResourceLocation id = itemRegistry.getKey(item);
        if (item instanceof BlockItem blockItem) {
            block = blockItem.getBlock();
        } else if (blockRegistry.getOptional(id).isPresent()) {
            block = blockRegistry.getValue(id);
        }

        if (block != null) {
            if (block instanceof AbstractBannerBlock) {
                return TextColor.color(appropriateColor(((AbstractBannerBlock) block).getColor().getMapColor().col));
            } else if (block instanceof BeaconBeamBlock) {
                return TextColor.color(appropriateColor(((BeaconBeamBlock) block).getColor().getMapColor().col));
            }
            return TextColor.color(appropriateColor(block.defaultMapColor().col));
        }
        return null;
    }

    public static int appropriateColor(int color) {
        if (color == 0) {
            return MapColor.SNOW.col;
        }
        int r = (color >> 16 & 255);
        int g = (color >> 8 & 255);
        int b = (color & 255);
        if (r < 70) r = 70;
        if (g < 70) g = 70;
        if (b < 70) b = 70;
        return (r << 16) + (g << 8) + b;
    }

    public long getTotalItems() {
        return counter.isEmpty() ? 0 : counter.values().longStream().sum();
    }

    public static HopperCounter getCounter(DyeColor color) {
        return COUNTERS.get(color);
    }

    public static void setEnabled(boolean is) {
        enabled = is;
    }

    public static boolean isEnabled() {
        return LeavesConfig.modify.hopperCounter && enabled;
    }
}
