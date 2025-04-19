package org.leavesmc.leaves.protocol.rei.display;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.rei.ingredient.EntryIngredient;

import java.util.List;

public abstract class CraftingDisplay extends Display {

    public CraftingDisplay(@NotNull List<EntryIngredient> inputs,
                           @NotNull List<EntryIngredient> outputs,
                           @NotNull ResourceLocation location) {
        super(inputs, outputs, location);
    }

    public abstract int getWidth();

    public abstract int getHeight();

}
