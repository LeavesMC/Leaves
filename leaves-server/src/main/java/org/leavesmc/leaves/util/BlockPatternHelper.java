package org.leavesmc.leaves.util;

import com.google.common.cache.LoadingCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;

// Powered by Carpet-AMS-Addition(https://github.com/Minecraft-AMS/Carpet-AMS-Addition)
public class BlockPatternHelper {
    public static BlockPattern.BlockPatternMatch partialSearchAround(BlockPattern pattern, Level world, BlockPos pos) {
        LoadingCache<BlockPos, BlockInWorld> loadingCache = BlockPattern.createLevelCache(world, false);
        int i = Math.max(Math.max(pattern.getWidth(), pattern.getHeight()), pattern.getDepth());

        for (BlockPos blockPos : BlockPos.betweenClosed(pos, pos.offset(i - 1, 0, i - 1))) {
            for (Direction direction : Direction.values()) {
                for (Direction direction2 : Direction.values()) {
                    BlockPattern.BlockPatternMatch result;
                    if (direction2 == direction || direction2 == direction.getOpposite() || (result = pattern.matches(blockPos, direction, direction2, loadingCache)) == null) {
                        continue;
                    }
                    return result;
                }
            }
        }
        return null;
    }
}
