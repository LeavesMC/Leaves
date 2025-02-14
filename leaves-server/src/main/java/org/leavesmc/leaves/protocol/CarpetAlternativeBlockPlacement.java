package org.leavesmc.leaves.protocol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

public class CarpetAlternativeBlockPlacement {

    @Nullable
    public static BlockState alternativeBlockPlacement(@NotNull Block block, @NotNull BlockPlaceContext context) {
        Vec3 hitPos = context.getClickLocation();
        BlockPos blockPos = context.getClickedPos();
        double relativeHitX = hitPos.x - blockPos.getX();
        BlockState state = block.getStateForPlacement(context);
        Player player = context.getPlayer();

        if (relativeHitX < 2 || state == null || player == null) {
            return null;
        }

        EnumProperty<Direction> directionProp = getFirstDirectionProperty(state);
        int protocolValue = ((int) relativeHitX - 2) / 2;

        if (directionProp != null) {
            Direction origFacing = state.getValue(directionProp);
            Direction facing = origFacing;
            int facingIndex = protocolValue & 0xF;

            if (facingIndex == 6) {
                facing = facing.getOpposite();
            } else if (facingIndex <= 5) {
                facing = Direction.from3DDataValue(facingIndex);
            }

            if (!directionProp.getPossibleValues().contains(facing)) {
                facing = player.getDirection().getOpposite();
            }

            if (facing != origFacing && directionProp.getPossibleValues().contains(facing)) {
                if (state.getBlock() instanceof BedBlock) {
                    BlockPos headPos = blockPos.relative(facing);

                    if (!context.getLevel().getBlockState(headPos).canBeReplaced(context)) {
                        return null;
                    }
                }

                state = state.setValue(directionProp, facing);
            }
        } else if (state.hasProperty(BlockStateProperties.AXIS)) {
            Direction.Axis axis = Direction.Axis.VALUES[protocolValue % 3];
            state = state.setValue(BlockStateProperties.AXIS, axis);
        }

        protocolValue &= 0xFFFFFFF0;

        if (protocolValue >= 16) {
            if (block instanceof RepeaterBlock) {
                Integer delay = (protocolValue / 16);

                if (RepeaterBlock.DELAY.getPossibleValues().contains(delay)) {
                    state = state.setValue(RepeaterBlock.DELAY, delay);
                }
            } else if (protocolValue == 16) {
                if (block instanceof ComparatorBlock) {
                    state = state.setValue(ComparatorBlock.MODE, ComparatorMode.SUBTRACT);
                } else if (state.hasProperty(BlockStateProperties.HALF) && state.getValue(BlockStateProperties.HALF) == Half.BOTTOM) {
                    state = state.setValue(BlockStateProperties.HALF, Half.TOP);
                } else if (state.hasProperty(BlockStateProperties.SLAB_TYPE) && state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM) {
                    state = state.setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP);
                }
            }
        }

        return state;
    }

    @Nullable
    public static BlockState alternativeBlockPlacementFix(Block block, @NotNull BlockPlaceContext context) {
        Direction facing;
        Vec3 vec3d = context.getClickLocation();
        BlockPos pos = context.getClickedPos();
        double hitX = vec3d.x - pos.getX();
        if (hitX < 2) {
            return null;
        }
        int code = (int) (hitX - 2) / 2;
        Player placer = Objects.requireNonNull(context.getPlayer());
        Level world = context.getLevel();

        if (block instanceof GlazedTerracottaBlock) {
            facing = Direction.from3DDataValue(code);
            if (facing == Direction.UP || facing == Direction.DOWN) {
                facing = placer.getDirection().getOpposite();
            }
            return block.defaultBlockState().setValue(GlazedTerracottaBlock.FACING, facing);
        } else if (block instanceof ObserverBlock) {
            return block.defaultBlockState().setValue(ObserverBlock.FACING, Direction.from3DDataValue(code)).setValue(ObserverBlock.POWERED, true);
        } else if (block instanceof RepeaterBlock) {
            facing = Direction.from3DDataValue(code % 16);
            if (facing == Direction.UP || facing == Direction.DOWN) {
                facing = placer.getDirection().getOpposite();
            }
            return block.defaultBlockState().setValue(RepeaterBlock.FACING, facing).setValue(RepeaterBlock.DELAY, Mth.clamp(code / 16, 1, 4)).setValue(RepeaterBlock.LOCKED, Boolean.FALSE);
        } else if (block instanceof TrapDoorBlock) {
            facing = Direction.from3DDataValue(code % 16);
            if (facing == Direction.UP || facing == Direction.DOWN) {
                facing = placer.getDirection().getOpposite();
            }
            return block.defaultBlockState().setValue(TrapDoorBlock.FACING, facing).setValue(TrapDoorBlock.OPEN, Boolean.FALSE).setValue(TrapDoorBlock.HALF, (code >= 16) ? Half.TOP : Half.BOTTOM).setValue(TrapDoorBlock.OPEN, world.hasNeighborSignal(pos));
        } else if (block instanceof ComparatorBlock) {
            facing = Direction.from3DDataValue(code % 16);
            if ((facing == Direction.UP) || (facing == Direction.DOWN)) {
                facing = placer.getDirection().getOpposite();
            }
            ComparatorMode m = (hitX >= 16) ? ComparatorMode.SUBTRACT : ComparatorMode.COMPARE;
            return block.defaultBlockState().setValue(ComparatorBlock.FACING, facing).setValue(ComparatorBlock.POWERED, Boolean.FALSE).setValue(ComparatorBlock.MODE, m);
        } else if (block instanceof DispenserBlock) {
            return block.defaultBlockState().setValue(DispenserBlock.FACING, Direction.from3DDataValue(code)).setValue(DispenserBlock.TRIGGERED, Boolean.FALSE);
        } else if (block instanceof PistonBaseBlock) {
            return block.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.from3DDataValue(code)).setValue(PistonBaseBlock.EXTENDED, Boolean.FALSE);
        } else if (block instanceof StairBlock) {
            return Objects.requireNonNull(block.getStateForPlacement(context)).setValue(StairBlock.FACING, Direction.from3DDataValue(code % 16)).setValue(StairBlock.HALF, (hitX >= 16) ? Half.TOP : Half.BOTTOM);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static EnumProperty<Direction> getFirstDirectionProperty(@NotNull BlockState state) {
        for (Property<?> prop : state.getProperties()) {
            if (prop instanceof EnumProperty<?> enumProperty) {
                if (enumProperty.getValueClass().equals(Direction.class)) {
                    return (EnumProperty<Direction>) enumProperty;
                }
            }
        }

        return null;
    }
}
