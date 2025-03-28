package org.leavesmc.leaves.protocol.servux.litematics.schematic.container;

import net.minecraft.world.level.block.state.BlockState;

public interface ILitematicaBlockStatePaletteResizer
{
    int onResize(int bits, BlockState state);
}
