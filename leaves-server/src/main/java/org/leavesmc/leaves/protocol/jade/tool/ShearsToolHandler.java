package org.leavesmc.leaves.protocol.jade.tool;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.leavesmc.leaves.protocol.jade.JadeProtocol;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ShearsToolHandler extends SimpleToolHandler {

    private static final ShearsToolHandler INSTANCE = new ShearsToolHandler();

    public static ShearsToolHandler getInstance() {
        return INSTANCE;
    }

    private final Set<Block> shearableBlocks = Sets.newIdentityHashSet();

    public ShearsToolHandler() {
        super(JadeProtocol.id("shears"), List.of(Items.SHEARS.getDefaultInstance()), true);
    }

    @Override
    public ItemStack test(BlockState state, Level world, BlockPos pos) {
        if (state.is(Blocks.TRIPWIRE) || shearableBlocks.contains(state.getBlock())) {
            return tools.getFirst();
        }
        return super.test(state, world, pos);
    }

    public void setShearableBlocks(Collection<Block> blocks) {
        shearableBlocks.clear();
        shearableBlocks.addAll(blocks);
    }
}
