package org.leavesmc.leaves.protocol.servux.litematics.selection;

import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public record Box(@Nullable BlockPos pos1, @Nullable BlockPos pos2, String name) {
    public Box() {
        this(BlockPos.ZERO, BlockPos.ZERO, "Unnamed");
    }

    @Nullable
    public BlockBox toVanilla() {
        if (pos1 != null && pos2 != null) {
            return new BlockBox(pos1, pos2);
        }
        return null;
    }
}
