package org.leavesmc.leaves.protocol.rei.display;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CampfireDisplay extends CookingDisplay {
    public CampfireDisplay(RecipeHolder<CampfireCookingRecipe> recipe) {
        super(recipe);
    }

    private static final ResourceLocation SERIALIZER_ID = ResourceLocation.tryBuild("minecraft", "default/campfire");

    @Override
    public ResourceLocation getSerializerId() {
        return SERIALIZER_ID;
    }
}
