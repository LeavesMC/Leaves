// LeavesMC - Protocol support for Servux mod
// Copyright (c) 2024 LeavesMC
// Original source: https://github.com/RelativityMC/servux (LGPL-3.0 License)
// This file is licensed under the GNU General Public License v3.0.

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
