package org.leavesmc.leaves.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public class UpdateSuppressionError extends Error {

    private final BlockPos pos;
    private final Block source;

    public UpdateSuppressionError(BlockPos pos, Block source) {
        super("Update suppression");
        this.pos = pos;
        this.source = source;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Block getSource() {
        return source;
    }

    public String getMessage() {
        if (pos != null) {
            return "An update suppression processed, form [%s] to [x:%d,y:%d,z:%d]".formatted(source.getName(), pos.getX(), pos.getY(), pos.getZ());
        } else {
            return "An update suppression processed, form [%s]".formatted(source.getName());
        }
    }
}
