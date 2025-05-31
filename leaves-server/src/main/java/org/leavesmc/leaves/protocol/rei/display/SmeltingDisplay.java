package org.leavesmc.leaves.protocol.rei.display;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public class SmeltingDisplay extends CookingDisplay {
    private static final ResourceLocation SERIALIZER_ID = ResourceLocation.tryBuild("minecraft", "default/smelting");

    public SmeltingDisplay(RecipeHolder<SmeltingRecipe> recipe) {
        super(recipe);
    }

    @Override
    public ResourceLocation getSerializerId() {
        return SERIALIZER_ID;
    }
}
