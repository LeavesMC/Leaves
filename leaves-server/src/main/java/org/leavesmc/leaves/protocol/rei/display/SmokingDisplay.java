package org.leavesmc.leaves.protocol.rei.display;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class SmokingDisplay extends CookingDisplay {
    public SmokingDisplay(RecipeHolder<? extends AbstractCookingRecipe> recipe) {
        super(recipe);
    }

    private static final ResourceLocation SERIALIZER_ID = ResourceLocation.tryBuild("minecraft", "default/smoking");

    @Override
    public ResourceLocation getSerializerId() {
        return SERIALIZER_ID;
    }
}
