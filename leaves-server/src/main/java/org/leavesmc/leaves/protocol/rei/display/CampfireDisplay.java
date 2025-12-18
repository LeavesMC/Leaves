package org.leavesmc.leaves.protocol.rei.display;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CampfireDisplay extends CookingDisplay {
    private static final Identifier SERIALIZER_ID = Identifier.tryBuild("minecraft", "default/campfire");

    public CampfireDisplay(RecipeHolder<CampfireCookingRecipe> recipe) {
        super(recipe);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER_ID;
    }
}
