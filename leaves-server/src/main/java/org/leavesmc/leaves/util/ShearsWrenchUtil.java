package org.leavesmc.leaves.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;

import java.util.List;

import static net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE;

public class ShearsWrenchUtil {

    public static InteractionResult tryApplyRotate(UseOnContext context, BlockState blockState) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Block block = blockState.getBlock();
        if (!LeavesConfig.modify.redstoneShearsWrench || !(block instanceof ObserverBlock || block instanceof DispenserBlock || block instanceof PistonBaseBlock || block instanceof HopperBlock || block instanceof RepeaterBlock || block instanceof ComparatorBlock || block instanceof CrafterBlock || block instanceof LeverBlock || block instanceof CocoaBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock || block instanceof LightningRodBlock || block instanceof CalibratedSculkSensorBlock || block instanceof BaseRailBlock)) {
            return null;
        }
        if (context.getPlayer() == null || !context.getPlayer().getItemInHand(invert(context.getHand())).isEmpty()) {
            return null;
        }
        StateDefinition<@NotNull Block, @NotNull BlockState> blockstatelist = block.getStateDefinition();
        Property<?> iblockstate;
        if (block instanceof CrafterBlock) {
            iblockstate = blockstatelist.getProperty("orientation");
        } else if (block instanceof BaseRailBlock) {
            iblockstate = blockstatelist.getProperty("shape");
        } else {
            iblockstate = blockstatelist.getProperty("facing");
        }
        Player player = context.getPlayer();

        if (iblockstate == null || player == null) {
            return InteractionResult.FAIL;
        }

        if (block instanceof BaseRailBlock) {
            if (block instanceof RailBlock) {
                if (blockState.getValue(RailBlock.SHAPE).isSlope()) {
                    return InteractionResult.FAIL;
                }
            } else {
                if (getNameHelper(blockState, PoweredRailBlock.POWERED).equals("true")) {
                    return InteractionResult.FAIL;
                }
                if (blockState.getValue(PoweredRailBlock.SHAPE).isSlope()) {
                    return InteractionResult.FAIL;
                }
            }
        }

        if (block instanceof PistonBaseBlock) {
            if (getNameHelper(blockState, PistonBaseBlock.EXTENDED).equals("true")) {
                return InteractionResult.FAIL;
            }
        }

        if (block instanceof RepeaterBlock || block instanceof ComparatorBlock) {
            if (getNameHelper(blockState, ComparatorBlock.POWERED).equals("true")) {
                return InteractionResult.FAIL;
            }
            if (block instanceof RepeaterBlock) {
                if (getNameHelper(blockState, RepeaterBlock.LOCKED).equals("true")) {
                    return InteractionResult.FAIL;
                }
            }
        }

        if (block instanceof CrafterBlock) {
            if (getNameHelper(blockState, CrafterBlock.CRAFTING).equals("true")) {
                return InteractionResult.FAIL;
            }
        }

        BlockState iblockdata1 = cycleState(blockState, iblockstate, player.isSecondaryUseActive());
        level.setBlock(clickedPos, iblockdata1, Block.UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE);
        message(player, Component.translatable("item.minecraft.debug_stick.update", iblockstate.getName(), getNameHelper(iblockdata1, iblockstate)));
        return InteractionResult.CONSUME;
    }

    private static InteractionHand invert(InteractionHand original) {
        return original == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private static <T extends Comparable<T>> BlockState cycleState(BlockState state, Property<@NotNull T> property, boolean inverse) {
        List<T> possibleValues = property.getPossibleValues();
        if (possibleValues.getFirst() instanceof RailShape) {
            boolean isRailBlock = state.getBlock() instanceof RailBlock;
            possibleValues = possibleValues.stream().filter(possibleValue -> {
                RailShape shape = (RailShape) possibleValue;
                if (isRailBlock) {
                    return !shape.isSlope();
                }
                return !shape.isSlope() && shape != RailShape.NORTH_EAST && shape != RailShape.NORTH_WEST && shape != RailShape.SOUTH_EAST && shape != RailShape.SOUTH_WEST;
            }).toList();
        }
        return state.setValue(property, getRelative(possibleValues, state.getValue(property), inverse)); // CraftBukkit - decompile error
    }

    private static <T> T getRelative(Iterable<T> elements, T current, boolean inverse) {
        return inverse ? Util.findPreviousInIterable(elements, current) : Util.findNextInIterable(elements, current);
    }

    private static void message(Player player, Component message) {
        ((ServerPlayer) player).sendSystemMessage(message, true);
    }

    private static <T extends Comparable<T>> String getNameHelper(BlockState state, Property<@NotNull T> property) {
        return property.getName(state.getValue(property));
    }
}