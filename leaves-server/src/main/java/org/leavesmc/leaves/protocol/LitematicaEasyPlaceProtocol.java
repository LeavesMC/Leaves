package org.leavesmc.leaves.protocol;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesLogger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LitematicaEasyPlaceProtocol {

    public static final ImmutableSet<Property<?>> WHITELISTED_PROPERTIES = ImmutableSet.of(
        BlockStateProperties.INVERTED,
        BlockStateProperties.OPEN,
        BlockStateProperties.BELL_ATTACHMENT,
        BlockStateProperties.AXIS,
        BlockStateProperties.BED_PART,
        BlockStateProperties.HALF,
        BlockStateProperties.ATTACH_FACE,
        BlockStateProperties.CHEST_TYPE,
        BlockStateProperties.MODE_COMPARATOR,
        BlockStateProperties.DOOR_HINGE,
        BlockStateProperties.FACING_HOPPER,
        BlockStateProperties.HORIZONTAL_FACING,
        BlockStateProperties.ORIENTATION,
        BlockStateProperties.RAIL_SHAPE,
        BlockStateProperties.RAIL_SHAPE_STRAIGHT,
        BlockStateProperties.SLAB_TYPE,
        BlockStateProperties.STAIRS_SHAPE,
        BlockStateProperties.BITES,
        BlockStateProperties.DELAY,
        BlockStateProperties.NOTE,
        BlockStateProperties.ROTATION_16
    );

    public static final ImmutableMap<Property<?>, ?> BLACKLISTED_PROPERTIES = ImmutableMap.of(
        BlockStateProperties.WATERLOGGED, false,
        BlockStateProperties.POWERED, false
    );

    public static BlockState applyPlacementProtocol(BlockState state, BlockPlaceContext context) {
        return applyPlacementProtocolV3(state, UseContext.from(context, context.getHand()));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <T extends Comparable<T>> BlockState applyPlacementProtocolV3(BlockState state, @NotNull UseContext context) {
        int protocolValue = (int) (context.getHitVec().x - (double) context.getPos().getX()) - 2;
        BlockState oldState = state;
        if (protocolValue < 0) {
            return oldState;
        }

        EnumProperty<Direction> property = CarpetAlternativeBlockPlacement.getFirstDirectionProperty(state);

        if (property != null && property != BlockStateProperties.VERTICAL_DIRECTION) {
            state = applyDirectionProperty(state, context, property, protocolValue);

            if (state == null) {
                return null;
            }

            if (state.canSurvive(context.getWorld(), context.getPos())) {
                oldState = state;
            } else {
                state = oldState;
            }

            protocolValue >>>= 3;
        }

        protocolValue >>>= 1;

        List<Property<?>> propList = new ArrayList<>(state.getBlock().getStateDefinition().getProperties());
        propList.sort(Comparator.comparing(Property::getName));

        try {
            for (Property<?> p : propList) {
                if (property != null && property.equals(p)) {
                    continue;
                }
                if (!WHITELISTED_PROPERTIES.contains(p) || BLACKLISTED_PROPERTIES.containsKey(p)) {
                    continue;
                }

                Property<T> prop = (Property<T>) p;
                List<T> list = new ArrayList<>(prop.getPossibleValues());
                list.sort(Comparable::compareTo);

                int requiredBits = Mth.log2(Mth.smallestEncompassingPowerOfTwo(list.size()));
                int bitMask = ~(0xFFFFFFFF << requiredBits);
                int valueIndex = protocolValue & bitMask;

                if (valueIndex < list.size()) {
                    T value = list.get(valueIndex);

                    if (!state.getValue(prop).equals(value) && value != SlabType.DOUBLE) {
                        state = state.setValue(prop, value);

                        if (state.canSurvive(context.getWorld(), context.getPos())) {
                            oldState = state;
                        } else {
                            state = oldState;
                        }
                    }

                    protocolValue >>>= requiredBits;
                }
            }
        } catch (Exception e) {
            LeavesLogger.LOGGER.warning("Exception trying to apply placement protocol value", e);
        }

        for (Map.Entry<Property<?>, ?> p : BLACKLISTED_PROPERTIES.entrySet()) {
            if (state.hasProperty(p.getKey())) {
                state = state.setValue((Property<T>) p.getKey(), (T) p.getValue());
            }
        }

        if (state.canSurvive(context.getWorld(), context.getPos())) {
            return state;
        } else {
            return null;
        }
    }

    private static BlockState applyDirectionProperty(BlockState state, UseContext context, EnumProperty<Direction> property, int protocolValue) {
        Direction facingOrig = state.getValue(property);
        Direction facing = facingOrig;
        int decodedFacingIndex = (protocolValue & 0xF) >> 1;

        if (decodedFacingIndex == 6) {
            facing = facing.getOpposite();
        } else if (decodedFacingIndex <= 5) {
            facing = Direction.from3DDataValue(decodedFacingIndex);

            if (!property.getPossibleValues().contains(facing)) {
                facing = context.getEntity().getDirection().getOpposite();
            }
        }

        if (facing != facingOrig && property.getPossibleValues().contains(facing)) {
            if (state.getBlock() instanceof BedBlock) {
                BlockPos headPos = context.pos.relative(facing);
                BlockPlaceContext ctx = context.getItemPlacementContext();

                if (ctx == null || !context.getWorld().getBlockState(headPos).canBeReplaced(ctx)) {
                    return null;
                }
            }

            state = state.setValue(property, facing);
        }

        return state;
    }

    public static class UseContext {

        private final Level world;
        private final BlockPos pos;
        private final Direction side;
        private final Vec3 hitVec;
        private final LivingEntity entity;
        private final InteractionHand hand;
        @Nullable
        private final BlockPlaceContext itemPlacementContext;

        private UseContext(Level world, BlockPos pos, Direction side, Vec3 hitVec, LivingEntity entity, InteractionHand hand, @Nullable BlockPlaceContext itemPlacementContext) {
            this.world = world;
            this.pos = pos;
            this.side = side;
            this.hitVec = hitVec;
            this.entity = entity;
            this.hand = hand;
            this.itemPlacementContext = itemPlacementContext;
        }

        @NotNull
        public static UseContext from(@NotNull BlockPlaceContext ctx, InteractionHand hand) {
            Vec3 pos = ctx.getClickLocation();
            return new UseContext(ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace(), new Vec3(pos.x, pos.y, pos.z), ctx.getPlayer(), hand, ctx);
        }

        public Level getWorld() {
            return this.world;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public Direction getSide() {
            return this.side;
        }

        public Vec3 getHitVec() {
            return this.hitVec;
        }

        public LivingEntity getEntity() {
            return this.entity;
        }

        public InteractionHand getHand() {
            return this.hand;
        }

        @Nullable
        public BlockPlaceContext getItemPlacementContext() {
            return this.itemPlacementContext;
        }
    }
}
