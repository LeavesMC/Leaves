package org.leavesmc.leaves.protocol.rei.display;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class SmokingDisplay extends CookingDisplay {
    private static final Identifier SERIALIZER_ID = Identifier.tryBuild("minecraft", "default/smoking");

    public SmokingDisplay(RecipeHolder<? extends AbstractCookingRecipe> recipe) {
        super(recipe);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER_ID;
    }
}
