// LeavesMC - Protocol support for Servux mod
// Copyright (c) 2024 LeavesMC
// Original source: https://github.com/RelativityMC/servux (LGPL-3.0 License)
// This file is licensed under the GNU General Public License v3.0.

package org.leavesmc.leaves.protocol.servux.litematics.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.leavesmc.leaves.protocol.servux.ServuxProtocol;

public record SubRegionPlacement(
    String name,
    BlockPos pos,
    Rotation rotation,
    Mirror mirror,
    boolean enabled,
    boolean ignoreEntities
) {
    public SubRegionPlacement(String name, BlockPos pos) {
        this(name, pos, Rotation.NONE, Mirror.NONE, true, false);
    }

    public boolean matchesRequirement(RequiredEnabled required) {
        if (required == RequiredEnabled.ANY) {
            return true;
        }

        if (required == RequiredEnabled.PLACEMENT_ENABLED) {
            return this.enabled();
        }

        ServuxProtocol.LOGGER.warn("RequiredEnabled.RENDERING_ENABLED is not supported on server side!");
        return false;
    }

    public enum RequiredEnabled {
        ANY,
        PLACEMENT_ENABLED,
        RENDERING_ENABLED
    }
}