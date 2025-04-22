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