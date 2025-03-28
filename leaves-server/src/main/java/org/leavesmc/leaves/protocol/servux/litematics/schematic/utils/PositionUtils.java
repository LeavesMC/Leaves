package org.leavesmc.leaves.protocol.servux.litematics.schematic.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.leavesmc.leaves.protocol.servux.litematics.malilib.IntBoundingBox;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.placement.SchematicPlacement;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.placement.SubRegionPlacement;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.selection.AreaSelection;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.selection.Box;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public class PositionUtils {
    public static final Direction[] ALL_DIRECTIONS = new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    public static final Direction[] HORIZONTAL_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    public static final Direction[] VERTICAL_DIRECTIONS = new Direction[]{Direction.DOWN, Direction.UP};

    public static Vec3 modifyValue(CoordinateType type, Vec3 valueIn, double amount) {
        switch (type) {
            case X:
                return new Vec3(valueIn.x + amount, valueIn.y, valueIn.z);
            case Y:
                return new Vec3(valueIn.x, valueIn.y + amount, valueIn.z);
            case Z:
                return new Vec3(valueIn.x, valueIn.y, valueIn.z + amount);
        }

        return valueIn;
    }

    public static BlockPos modifyValue(CoordinateType type, BlockPos valueIn, int amount) {
        switch (type) {
            case X:
                return BlockPos.containing(valueIn.getX() + amount, valueIn.getY(), valueIn.getZ());
            case Y:
                return BlockPos.containing(valueIn.getX(), valueIn.getY() + amount, valueIn.getZ());
            case Z:
                return BlockPos.containing(valueIn.getX(), valueIn.getY(), valueIn.getZ() + amount);
        }

        return valueIn;
    }

    public static Vec3 setValue(CoordinateType type, Vec3 valueIn, double newValue) {
        switch (type) {
            case X:
                return new Vec3(newValue, valueIn.y, valueIn.z);
            case Y:
                return new Vec3(valueIn.x, newValue, valueIn.z);
            case Z:
                return new Vec3(valueIn.x, valueIn.y, newValue);
        }

        return valueIn;
    }

    public static BlockPos setValue(CoordinateType type, BlockPos valueIn, int newValue) {
        switch (type) {
            case X:
                return BlockPos.containing(newValue, valueIn.getY(), valueIn.getZ());
            case Y:
                return BlockPos.containing(valueIn.getX(), newValue, valueIn.getZ());
            case Z:
                return BlockPos.containing(valueIn.getX(), valueIn.getY(), newValue);
        }

        return valueIn;
    }

    public static BlockPos getEntityBlockPos(Entity entity) {
        return BlockPos.containing(Math.floor(entity.getX()), Math.floor(entity.getY()), Math.floor(entity.getZ()));
    }

    /**
     * Returns the closest direction the given entity is looking towards,
     * with a vertical/pitch threshold of 60 degrees.
     *
     * @param entity
     * @return
     */
    public static Direction getClosestLookingDirection(Entity entity) {
        return getClosestLookingDirection(entity, 60);
    }

    /**
     * Returns the closest direction the given entity is looking towards.
     *
     * @param entity
     * @param verticalThreshold the pitch threshold to return the up or down facing instead of horizontals
     * @return
     */
    public static Direction getClosestLookingDirection(Entity entity, float verticalThreshold) {
        if (entity.getXRot() >= verticalThreshold) {
            return Direction.DOWN;
        } else if (entity.getYRot() <= -verticalThreshold) {
            return Direction.UP;
        }

        return entity.getDirection();
    }

    /**
     * Returns the closest block position directly infront of the
     * given entity that is not colliding with it.
     *
     * @param entity
     * @return
     */
    public static BlockPos getPositionInfrontOfEntity(Entity entity) {
        return getPositionInfrontOfEntity(entity, 60);
    }

    /**
     * Returns the closest block position directly infront of the
     * given entity that is not colliding with it.
     *
     * @param entity
     * @param verticalThreshold
     * @return
     */
    public static BlockPos getPositionInfrontOfEntity(Entity entity, float verticalThreshold) {
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        double w = entity.getBbWidth();
        BlockPos pos = BlockPos.containing(x, y, z);

        if (entity.getXRot() >= verticalThreshold) {
            return pos.below(1);
        } else if (entity.getXRot() <= -verticalThreshold) {
            return BlockPos.containing(x, Math.ceil(entity.getBoundingBox().maxY), z);
        }

        y = Math.floor(y + entity.getEyeHeight());

        switch (entity.getDirection()) {
            case EAST:
                return BlockPos.containing((int) Math.ceil(x + w / 2), (int) y, (int) Math.floor(z));
            case WEST:
                return BlockPos.containing((int) Math.floor(x - w / 2) - 1, (int) y, (int) Math.floor(z));
            case SOUTH:
                return BlockPos.containing((int) Math.floor(x), (int) y, (int) Math.ceil(z + w / 2));
            case NORTH:
                return BlockPos.containing((int) Math.floor(x), (int) y, (int) Math.floor(z - w / 2) - 1);
            default:
        }

        return pos;
    }

    /**
     * Returns the hit vector at the center point of the given side/face of the given block position.
     *
     * @param basePos
     * @param facing
     * @return
     */
    public static Vec3 getHitVecCenter(BlockPos basePos, Direction facing) {
        int x = basePos.getX();
        int y = basePos.getY();
        int z = basePos.getZ();

        switch (facing) {
            case UP:
                return new Vec3(x + 0.5, y + 1, z + 0.5);
            case DOWN:
                return new Vec3(x + 0.5, y, z + 0.5);
            case NORTH:
                return new Vec3(x + 0.5, y + 0.5, z);
            case SOUTH:
                return new Vec3(x + 0.5, y + 0.5, z + 1);
            case WEST:
                return new Vec3(x, y + 0.5, z);
            case EAST:
                return new Vec3(x + 1, y + 0.5, z + 1);
            default:
                return new Vec3(x, y, z);
        }
    }

    /**
     * Returns the part of the block face the player is currently targeting.
     * The block face is divided into four side segments and a center segment.
     *
     * @param originalSide
     * @param playerFacingH
     * @param pos
     * @param hitVec
     * @return
     */
    public static HitPart getHitPart(Direction originalSide, Direction playerFacingH, BlockPos pos, Vec3 hitVec) {
        Vec3 positions = getHitPartPositions(originalSide, playerFacingH, pos, hitVec);
        double posH = positions.x;
        double posV = positions.y;
        double offH = Math.abs(posH - 0.5d);
        double offV = Math.abs(posV - 0.5d);

        if (offH > 0.25d || offV > 0.25d) {
            if (offH > offV) {
                return posH < 0.5d ? HitPart.LEFT : HitPart.RIGHT;
            } else {
                return posV < 0.5d ? HitPart.BOTTOM : HitPart.TOP;
            }
        } else {
            return HitPart.CENTER;
        }
    }

    private static Vec3 getHitPartPositions(Direction originalSide, Direction playerFacingH, BlockPos pos, Vec3 hitVec) {
        double x = hitVec.x - pos.getX();
        double y = hitVec.y - pos.getY();
        double z = hitVec.z - pos.getZ();
        double posH = 0;
        double posV = 0;

        switch (originalSide) {
            case DOWN:
            case UP:
                switch (playerFacingH) {
                    case NORTH:
                        posH = x;
                        posV = 1.0d - z;
                        break;
                    case SOUTH:
                        posH = 1.0d - x;
                        posV = z;
                        break;
                    case WEST:
                        posH = 1.0d - z;
                        posV = 1.0d - x;
                        break;
                    case EAST:
                        posH = z;
                        posV = x;
                        break;
                    default:
                }

                if (originalSide == Direction.DOWN) {
                    posV = 1.0d - posV;
                }

                break;
            case NORTH:
            case SOUTH:
                posH = originalSide.getAxisDirection() == Direction.AxisDirection.POSITIVE ? x : 1.0d - x;
                posV = y;
                break;
            case WEST:
            case EAST:
                posH = originalSide.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? z : 1.0d - z;
                posV = y;
                break;
        }

        return new Vec3(posH, posV, 0);
    }

    public static Direction rotateYCounterclockwise(Direction direction) {
        Direction var10000;
        switch (direction.ordinal()) {
            case 2 -> var10000 = Direction.WEST;
            case 3 -> var10000 = Direction.EAST;
            case 4 -> var10000 = Direction.SOUTH;
            case 5 -> var10000 = Direction.NORTH;
            default -> throw new IllegalStateException("Unable to get CCW facing of " + direction);
        }

        return var10000;
    }

    public static Direction rotateYClockwise(Direction direction) {
        Direction var10000;
        switch (direction.ordinal()) {
            case 2 -> var10000 = Direction.EAST;
            case 3 -> var10000 = Direction.WEST;
            case 4 -> var10000 = Direction.NORTH;
            case 5 -> var10000 = Direction.SOUTH;
            default -> throw new IllegalStateException("Unable to get Y-rotated facing of " + direction);
        }

        return var10000;
    }

    /**
     * Returns the direction the targeted part of the targeting overlay is pointing towards.
     *
     * @param side
     * @param playerFacingH
     * @param pos
     * @param hitVec
     * @return
     */
    public static Direction getTargetedDirection(Direction side, Direction playerFacingH, BlockPos pos, Vec3 hitVec) {
        Vec3 positions = getHitPartPositions(side, playerFacingH, pos, hitVec);
        double posH = positions.x;
        double posV = positions.y;
        double offH = Math.abs(posH - 0.5d);
        double offV = Math.abs(posV - 0.5d);

        if (offH > 0.25d || offV > 0.25d) {
            if (side.getAxis() == Direction.Axis.Y) {
                if (offH > offV) {
                    return posH < 0.5d ? rotateYCounterclockwise(playerFacingH) : rotateYClockwise(playerFacingH);
                } else {
                    if (side == Direction.DOWN) {
                        return posV > 0.5d ? playerFacingH.getOpposite() : playerFacingH;
                    } else {
                        return posV < 0.5d ? playerFacingH.getOpposite() : playerFacingH;
                    }
                }
            } else {
                if (offH > offV) {
                    return posH < 0.5d ? rotateYClockwise(side) : rotateYCounterclockwise(side);
                } else {
                    return posV < 0.5d ? Direction.DOWN : Direction.UP;
                }
            }
        }

        return side;
    }

    public enum HitPart {
        CENTER,
        LEFT,
        RIGHT,
        BOTTOM,
        TOP
    }

    public enum CoordinateType {
        X,
        Y,
        Z
    }

    public static final BlockPosComparator BLOCK_POS_COMPARATOR = new BlockPosComparator();
    public static final ChunkPosComparator CHUNK_POS_COMPARATOR = new ChunkPosComparator();

    public static final Direction.Axis[] AXES_ALL = new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z};
    public static final Direction[] ADJACENT_SIDES_ZY = new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};
    public static final Direction[] ADJACENT_SIDES_XY = new Direction[]{Direction.DOWN, Direction.UP, Direction.EAST, Direction.WEST};
    public static final Direction[] ADJACENT_SIDES_XZ = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private static final Vec3i[] EDGE_NEIGHBOR_OFFSETS_XN_ZN = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, -1), new Vec3i(-1, 0, -1)};
    private static final Vec3i[] EDGE_NEIGHBOR_OFFSETS_XP_ZN = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(1, 0, 0), new Vec3i(0, 0, -1), new Vec3i(1, 0, -1)};
    private static final Vec3i[] EDGE_NEIGHBOR_OFFSETS_XN_ZP = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(-1, 0, 1)};
    private static final Vec3i[] EDGE_NEIGHBOR_OFFSETS_XP_ZP = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(1, 0, 1)};
    private static final Vec3i[][] EDGE_NEIGHBOR_OFFSETS_Y = new Vec3i[][]{EDGE_NEIGHBOR_OFFSETS_XN_ZN, EDGE_NEIGHBOR_OFFSETS_XP_ZN, EDGE_NEIGHBOR_OFFSETS_XN_ZP, EDGE_NEIGHBOR_OFFSETS_XP_ZP};

    private static final Vec3i[] EDGE_NEIGHBOR_OFFSETS_XN_YN = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, -1, 0), new Vec3i(-1, -1, 0)};
    private static final Vec3i[] EDGE_NEIGHBOR_OFFSETS_XP_YN = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(1, 0, 0), new Vec3i(0, -1, 0), new Vec3i(1, -1, 0)};
    private static final Vec3i[] EDGE_NEIGHBOR_OFFSETS_XN_YP = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 1, 0), new Vec3i(-1, 1, 0)};
    private static final Vec3i[] EDGE_NEIGHBOR_OFFSETS_XP_YP = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(1, 0, 0), new Vec3i(0, 1, 0), new Vec3i(1, 1, 0)};
    private static final Vec3i[][] EDGE_NEIGHBOR_OFFSETS_Z = new Vec3i[][]{EDGE_NEIGHBOR_OFFSETS_XN_YN, EDGE_NEIGHBOR_OFFSETS_XP_YN, EDGE_NEIGHBOR_OFFSETS_XN_YP, EDGE_NEIGHBOR_OFFSETS_XP_YP};

    private static final Vec3i[] EDGE_NEIGHBOR_OFFSETS_YN_ZN = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(0, -1, 0), new Vec3i(0, 0, -1), new Vec3i(0, -1, -1)};
    private static final Vec3i[] EDGE_NEIGHBOR_OFFSETS_YP_ZN = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(0, 1, 0), new Vec3i(0, 0, -1), new Vec3i(0, 1, -1)};
    private static final Vec3i[] EDGE_NEIGHBOR_OFFSETS_YN_ZP = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(0, -1, 0), new Vec3i(0, 0, 1), new Vec3i(0, -1, 1)};
    private static final Vec3i[] EDGE_NEIGHBOR_OFFSETS_YP_ZP = new Vec3i[]{new Vec3i(0, 0, 0), new Vec3i(0, 1, 0), new Vec3i(0, 0, 1), new Vec3i(0, 1, 1)};
    private static final Vec3i[][] EDGE_NEIGHBOR_OFFSETS_X = new Vec3i[][]{EDGE_NEIGHBOR_OFFSETS_YN_ZN, EDGE_NEIGHBOR_OFFSETS_YP_ZN, EDGE_NEIGHBOR_OFFSETS_YN_ZP, EDGE_NEIGHBOR_OFFSETS_YP_ZP};

    public static Vec3i[] getEdgeNeighborOffsets(Direction.Axis axis, int cornerIndex) {
        switch (axis) {
            case X:
                return EDGE_NEIGHBOR_OFFSETS_X[cornerIndex];
            case Y:
                return EDGE_NEIGHBOR_OFFSETS_Y[cornerIndex];
            case Z:
                return EDGE_NEIGHBOR_OFFSETS_Z[cornerIndex];
        }

        return null;
    }

    public static long getChunkPosLong(BlockPos blockPos) {
        return ChunkPos.asLong(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    public static BlockPos getMinCorner(BlockPos pos1, BlockPos pos2) {
        return new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
    }

    public static BlockPos getMaxCorner(BlockPos pos1, BlockPos pos2) {
        return new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
    }

    public static boolean isPositionInsideArea(BlockPos pos, BlockPos posMin, BlockPos posMax) {
        return pos.getX() >= posMin.getX() && pos.getX() <= posMax.getX() &&
            pos.getY() >= posMin.getY() && pos.getY() <= posMax.getY() &&
            pos.getZ() >= posMin.getZ() && pos.getZ() <= posMax.getZ();
    }

    public static BlockPos getTransformedPlacementPosition(BlockPos posWithinSub, SchematicPlacement schematicPlacement, SubRegionPlacement placement) {
        BlockPos pos = posWithinSub;
        pos = getTransformedBlockPos(pos, schematicPlacement.getMirror(), schematicPlacement.getRotation());
        pos = getTransformedBlockPos(pos, placement.getMirror(), placement.getRotation());
        return pos;
    }

    public static boolean arePositionsWithinWorld(Level world, BlockPos pos1, BlockPos pos2) {
        if (pos1.getY() >= world.getMinY() && pos1.getY() < world.getMaxSectionY() &&
            pos2.getY() >= world.getMinY() && pos2.getY() < world.getMaxSectionY()) {
            WorldBorder border = world.getWorldBorder();
            return border.isWithinBounds(pos1) && border.isWithinBounds(pos2);
        }

        return false;
    }

    public static boolean isBoxWithinWorld(Level world, Box box) {
        if (box.getPos1() != null && box.getPos2() != null) {
            return arePositionsWithinWorld(world, box.getPos1(), box.getPos2());
        }

        return false;
    }

    public static BlockPos getAreaSizeFromRelativeEndPosition(BlockPos posEndRelative) {
        int x = posEndRelative.getX();
        int y = posEndRelative.getY();
        int z = posEndRelative.getZ();

        x = x >= 0 ? x + 1 : x - 1;
        y = y >= 0 ? y + 1 : y - 1;
        z = z >= 0 ? z + 1 : z - 1;

        return new BlockPos(x, y, z);
    }

    public static BlockPos getAreaSizeFromRelativeEndPositionAbs(BlockPos posEndRelative) {
        int x = posEndRelative.getX();
        int y = posEndRelative.getY();
        int z = posEndRelative.getZ();

        x = x >= 0 ? x + 1 : x - 1;
        y = y >= 0 ? y + 1 : y - 1;
        z = z >= 0 ? z + 1 : z - 1;

        return new BlockPos(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    public static BlockPos getRelativeEndPositionFromAreaSize(Vec3i size) {
        int x = size.getX();
        int y = size.getY();
        int z = size.getZ();

        x = x >= 0 ? x - 1 : x + 1;
        y = y >= 0 ? y - 1 : y + 1;
        z = z >= 0 ? z - 1 : z + 1;

        return new BlockPos(x, y, z);
    }

    public static List<Box> getValidBoxes(AreaSelection area) {
        List<Box> boxes = new ArrayList<>();
        Collection<Box> originalBoxes = area.getAllSubRegionBoxes();

        for (Box box : originalBoxes) {
            if (isBoxValid(box)) {
                boxes.add(box);
            }
        }

        return boxes;
    }

    public static boolean isBoxValid(Box box) {
        return box.getPos1() != null && box.getPos2() != null;
    }

    public static BlockPos getEnclosingAreaSize(AreaSelection area) {
        return getEnclosingAreaSize(area.getAllSubRegionBoxes());
    }

    public static BlockPos getEnclosingAreaSize(Collection<Box> boxes) {
        Pair<BlockPos, BlockPos> pair = getEnclosingAreaCorners(boxes);
        return pair.getRight().subtract(pair.getLeft()).offset(1, 1, 1);
    }

    /**
     * Returns the min and max corners of the enclosing box around the given collection of boxes.
     * The minimum corner is the left entry and the maximum corner is the right entry of the pair.
     *
     * @param boxes
     * @return
     */
    @Nullable
    public static Pair<BlockPos, BlockPos> getEnclosingAreaCorners(Collection<Box> boxes) {
        if (boxes.isEmpty()) {
            return null;
        }

        BlockPos.MutableBlockPos posMin = new BlockPos.MutableBlockPos(60000000, 60000000, 60000000);
        BlockPos.MutableBlockPos posMax = new BlockPos.MutableBlockPos(-60000000, -60000000, -60000000);

        for (Box box : boxes) {
            getMinMaxCoords(posMin, posMax, box.getPos1());
            getMinMaxCoords(posMin, posMax, box.getPos2());
        }

        return Pair.of(posMin.immutable(), posMax.immutable());
    }

    private static void getMinMaxCoords(BlockPos.MutableBlockPos posMin, BlockPos.MutableBlockPos posMax, @Nullable BlockPos posToCheck) {
        if (posToCheck != null) {
            posMin.set(Math.min(posMin.getX(), posToCheck.getX()),
                Math.min(posMin.getY(), posToCheck.getY()),
                Math.min(posMin.getZ(), posToCheck.getZ()));

            posMax.set(Math.max(posMax.getX(), posToCheck.getX()),
                Math.max(posMax.getY(), posToCheck.getY()),
                Math.max(posMax.getZ(), posToCheck.getZ()));
        }
    }

    @Nullable
    public static IntBoundingBox clampBoxToWorldHeightRange(IntBoundingBox box, Level world) {
        int minY = world.getMinY();
        int maxY = world.getMaxSectionY();

        if (box.minY > maxY || box.maxY < minY) {
            return null;
        }

        if (box.minY < minY || box.maxY > maxY) {
            box = new IntBoundingBox(box.minX, Math.max(box.minY, minY), box.minZ,
                box.maxX, Math.min(box.maxY, maxY), box.maxZ);
        }

        return box;
    }

    public static int getTotalVolume(Collection<Box> boxes) {
        if (boxes.isEmpty()) {
            return 0;
        }

        int volume = 0;

        for (Box box : boxes) {
            if (isBoxValid(box)) {
                BlockPos min = getMinCorner(box.getPos1(), box.getPos2());
                BlockPos max = getMaxCorner(box.getPos1(), box.getPos2());
                volume += (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
            }
        }

        return volume;
    }

    public static ImmutableMap<String, IntBoundingBox> getBoxesWithinChunk(int chunkX, int chunkZ, ImmutableMap<String, Box> subRegions) {
        ImmutableMap.Builder<String, IntBoundingBox> builder = new ImmutableMap.Builder<>();

        for (Map.Entry<String, Box> entry : subRegions.entrySet()) {
            Box box = entry.getValue();
            IntBoundingBox bb = box != null ? getBoundsWithinChunkForBox(box, chunkX, chunkZ) : null;

            if (bb != null) {
                builder.put(entry.getKey(), bb);
            }
        }

        return builder.build();
    }

    public static ImmutableList<IntBoundingBox> getBoxesWithinChunk(int chunkX, int chunkZ, Collection<Box> boxes) {
        ImmutableList.Builder<IntBoundingBox> builder = new ImmutableList.Builder<>();

        for (Box box : boxes) {
            IntBoundingBox bb = getBoundsWithinChunkForBox(box, chunkX, chunkZ);

            if (bb != null) {
                builder.add(bb);
            }
        }

        return builder.build();
    }

    public static Set<ChunkPos> getTouchedChunks(ImmutableMap<String, Box> boxes) {
        return getTouchedChunksForBoxes(boxes.values());
    }

    public static Set<ChunkPos> getTouchedChunksForBoxes(Collection<Box> boxes) {
        Set<ChunkPos> set = new HashSet<>();

        for (Box box : boxes) {
            final int boxXMin = Math.min(box.getPos1().getX(), box.getPos2().getX()) >> 4;
            final int boxZMin = Math.min(box.getPos1().getZ(), box.getPos2().getZ()) >> 4;
            final int boxXMax = Math.max(box.getPos1().getX(), box.getPos2().getX()) >> 4;
            final int boxZMax = Math.max(box.getPos1().getZ(), box.getPos2().getZ()) >> 4;

            for (int cz = boxZMin; cz <= boxZMax; ++cz) {
                for (int cx = boxXMin; cx <= boxXMax; ++cx) {
                    set.add(new ChunkPos(cx, cz));
                }
            }
        }

        return set;
    }

    @Nullable
    public static IntBoundingBox getBoundsWithinChunkForBox(Box box, int chunkX, int chunkZ) {
        final int chunkXMin = chunkX << 4;
        final int chunkZMin = chunkZ << 4;
        final int chunkXMax = chunkXMin + 15;
        final int chunkZMax = chunkZMin + 15;

        final int boxXMin = Math.min(box.getPos1().getX(), box.getPos2().getX());
        final int boxZMin = Math.min(box.getPos1().getZ(), box.getPos2().getZ());
        final int boxXMax = Math.max(box.getPos1().getX(), box.getPos2().getX());
        final int boxZMax = Math.max(box.getPos1().getZ(), box.getPos2().getZ());

        boolean notOverlapping = boxXMin > chunkXMax || boxZMin > chunkZMax || boxXMax < chunkXMin || boxZMax < chunkZMin;

        if (!notOverlapping) {
            final int xMin = Math.max(chunkXMin, boxXMin);
            final int yMin = Math.min(box.getPos1().getY(), box.getPos2().getY());
            final int zMin = Math.max(chunkZMin, boxZMin);
            final int xMax = Math.min(chunkXMax, boxXMax);
            final int yMax = Math.max(box.getPos1().getY(), box.getPos2().getY());
            final int zMax = Math.min(chunkZMax, boxZMax);

            return new IntBoundingBox(xMin, yMin, zMin, xMax, yMax, zMax);
        }

        return null;
    }

    public static void getPerChunkBoxes(Collection<Box> boxes, BiConsumer<ChunkPos, IntBoundingBox> consumer) {
        for (Box box : boxes) {
            final int boxMinX = Math.min(box.getPos1().getX(), box.getPos2().getX());
            final int boxMinY = Math.min(box.getPos1().getY(), box.getPos2().getY());
            final int boxMinZ = Math.min(box.getPos1().getZ(), box.getPos2().getZ());
            final int boxMaxX = Math.max(box.getPos1().getX(), box.getPos2().getX());
            final int boxMaxY = Math.max(box.getPos1().getY(), box.getPos2().getY());
            final int boxMaxZ = Math.max(box.getPos1().getZ(), box.getPos2().getZ());
            final int boxMinChunkX = boxMinX >> 4;
            final int boxMinChunkZ = boxMinZ >> 4;
            final int boxMaxChunkX = boxMaxX >> 4;
            final int boxMaxChunkZ = boxMaxZ >> 4;

            for (int cz = boxMinChunkZ; cz <= boxMaxChunkZ; ++cz) {
                for (int cx = boxMinChunkX; cx <= boxMaxChunkX; ++cx) {
                    final int chunkMinX = cx << 4;
                    final int chunkMinZ = cz << 4;
                    final int chunkMaxX = chunkMinX + 15;
                    final int chunkMaxZ = chunkMinZ + 15;
                    final int minX = Math.max(chunkMinX, boxMinX);
                    final int minZ = Math.max(chunkMinZ, boxMinZ);
                    final int maxX = Math.min(chunkMaxX, boxMaxX);
                    final int maxZ = Math.min(chunkMaxZ, boxMaxZ);

                    consumer.accept(new ChunkPos(cx, cz), new IntBoundingBox(minX, boxMinY, minZ, maxX, boxMaxY, maxZ));
                }
            }
        }
    }


    /**
     * Creates an enclosing AABB around the given positions. They will both be inside the box.
     */
    public static AABB createEnclosingAABB(BlockPos pos1, BlockPos pos2) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX()) + 1;
        int maxY = Math.max(pos1.getY(), pos2.getY()) + 1;
        int maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1;

        return createAABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static AABB createAABBFrom(IntBoundingBox bb) {
        return createAABB(bb.minX, bb.minY, bb.minZ, bb.maxX + 1, bb.maxY + 1, bb.maxZ + 1);
    }

    /**
     * Creates an AABB for the given position
     */
    public static AABB createAABBForPosition(BlockPos pos) {
        return createAABBForPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Creates an AABB for the given position
     */
    public static AABB createAABBForPosition(int x, int y, int z) {
        return createAABB(x, y, z, x + 1, y + 1, z + 1);
    }

    /**
     * Creates an AABB with the given bounds
     */
    public static AABB createAABB(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Returns the given position adjusted such that the coordinate indicated by <b>type</b>
     * is set to the value in <b>value</b>
     *
     * @param pos
     * @param value
     * @param type
     * @return
     */
    public static BlockPos getModifiedPosition(BlockPos pos, int value, CoordinateType type) {

        switch (type) {
            case X:
                pos = new BlockPos(value, pos.getY(), pos.getZ());
                break;
            case Y:
                pos = new BlockPos(pos.getX(), value, pos.getZ());
                break;
            case Z:
                pos = new BlockPos(pos.getX(), pos.getY(), value);
                break;
        }

        return pos;
    }

    public static int getCoordinate(BlockPos pos, CoordinateType type) {
        switch (type) {
            case X:
                return pos.getX();
            case Y:
                return pos.getY();
            case Z:
                return pos.getZ();
        }

        return 0;
    }

    public static Box growOrShrinkBox(Box box, int amount) {
        BlockPos pos1 = box.getPos1();
        BlockPos pos2 = box.getPos2();

        if (pos1 == null || pos2 == null) {
            if (pos1 == null && pos2 == null) {
                return box;
            } else if (pos2 == null) {
                pos2 = pos1;
            } else {
                pos1 = pos2;
            }
        }

        Pair<Integer, Integer> x = growCoordinatePair(pos1.getX(), pos2.getX(), amount);
        Pair<Integer, Integer> y = growCoordinatePair(pos1.getY(), pos2.getY(), amount);
        Pair<Integer, Integer> z = growCoordinatePair(pos1.getZ(), pos2.getZ(), amount);

        Box boxNew = box.copy();
        boxNew.setPos1(new BlockPos(x.getLeft(), y.getLeft(), z.getLeft()));
        boxNew.setPos2(new BlockPos(x.getRight(), y.getRight(), z.getRight()));

        return boxNew;
    }

    private static Pair<Integer, Integer> growCoordinatePair(int v1, int v2, int amount) {
        if (v2 >= v1) {
            if (v2 + amount >= v1) {
                v2 += amount;
            }

            if (v1 - amount <= v2) {
                v1 -= amount;
            }
        } else if (v1 > v2) {
            if (v1 + amount >= v2) {
                v1 += amount;
            }

            if (v2 - amount <= v1) {
                v2 -= amount;
            }
        }

        return Pair.of(v1, v2);
    }

    /**
     * Mirrors and then rotates the given position around the origin
     */
    public static BlockPos getTransformedBlockPos(BlockPos pos, Mirror mirror, Rotation rotation) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean isMirrored = true;

        switch (mirror) {
            // LEFT_RIGHT is essentially NORTH_SOUTH
            case LEFT_RIGHT:
                z = -z;
                break;
            // FRONT_BACK is essentially EAST_WEST
            case FRONT_BACK:
                x = -x;
                break;
            default:
                isMirrored = false;
        }

        switch (rotation) {
            case CLOCKWISE_90:
                return new BlockPos(-z, y, x);
            case COUNTERCLOCKWISE_90:
                return new BlockPos(z, y, -x);
            case CLOCKWISE_180:
                return new BlockPos(-x, y, -z);
            default:
                return isMirrored ? new BlockPos(x, y, z) : pos;
        }
    }

    public static BlockPos getReverseTransformedBlockPos(BlockPos pos, Mirror mirror, Rotation rotation) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean isRotated = true;
        int tmp = x;

        switch (rotation) {
            case CLOCKWISE_90:
                x = z;
                z = -tmp;
                break;
            case COUNTERCLOCKWISE_90:
                x = -z;
                z = tmp;
                break;
            case CLOCKWISE_180:
                x = -x;
                z = -z;
                break;
            default:
                isRotated = false;
        }

        switch (mirror) {
            // LEFT_RIGHT is essentially NORTH_SOUTH
            case LEFT_RIGHT:
                z = -z;
                break;
            // FRONT_BACK is essentially EAST_WEST
            case FRONT_BACK:
                x = -x;
                break;
            default:
                if (!isRotated) {
                    return pos;
                }
        }

        return new BlockPos(x, y, z);
    }

    /**
     * Does the opposite transform from getTransformedBlockPos(), to return the original,
     * non-transformed position from the transformed position.
     */
    public static BlockPos getOriginalPositionFromTransformed(BlockPos pos, Mirror mirror, Rotation rotation) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int tmp;
        boolean noRotation = false;

        switch (rotation) {
            case CLOCKWISE_90:
                tmp = x;
                x = -z;
                z = tmp;
            case COUNTERCLOCKWISE_90:
                tmp = x;
                x = z;
                z = -tmp;
            case CLOCKWISE_180:
                x = -x;
                z = -z;
            default:
                noRotation = true;
        }

        switch (mirror) {
            case LEFT_RIGHT:
                z = -z;
                break;
            case FRONT_BACK:
                x = -x;
                break;
            default:
                if (noRotation) {
                    return pos;
                }
        }

        return new BlockPos(x, y, z);
    }

    public static Vec3 getTransformedPosition(Vec3 originalPos, Mirror mirror, Rotation rotation) {
        double x = originalPos.x;
        double y = originalPos.y;
        double z = originalPos.z;
        boolean transformed = true;

        switch (mirror) {
            case LEFT_RIGHT:
                z = 1.0D - z;
                break;
            case FRONT_BACK:
                x = 1.0D - x;
                break;
            default:
                transformed = false;
        }

        switch (rotation) {
            case COUNTERCLOCKWISE_90:
                return new Vec3(z, y, 1.0D - x);
            case CLOCKWISE_90:
                return new Vec3(1.0D - z, y, x);
            case CLOCKWISE_180:
                return new Vec3(1.0D - x, y, 1.0D - z);
            default:
                return transformed ? new Vec3(x, y, z) : originalPos;
        }
    }

    public static Rotation getReverseRotation(Rotation rotationIn) {
        switch (rotationIn) {
            case COUNTERCLOCKWISE_90:
                return Rotation.CLOCKWISE_90;
            case CLOCKWISE_90:
                return Rotation.COUNTERCLOCKWISE_90;
            case CLOCKWISE_180:
                return Rotation.CLOCKWISE_180;
            default:
                return rotationIn;
        }
    }

    public static BlockPos getModifiedPartiallyLockedPosition(BlockPos posOriginal, BlockPos posNew, int lockMask) {
        if (lockMask != 0) {
            int x = posNew.getX();
            int y = posNew.getY();
            int z = posNew.getZ();

            if ((lockMask & (0x1 << CoordinateType.X.ordinal())) != 0) {
                x = posOriginal.getX();
            }

            if ((lockMask & (0x1 << CoordinateType.Y.ordinal())) != 0) {
                y = posOriginal.getY();
            }

            if ((lockMask & (0x1 << CoordinateType.Z.ordinal())) != 0) {
                z = posOriginal.getZ();
            }

            posNew = new BlockPos(x, y, z);
        }

        return posNew;
    }

    /**
     * Gets the "front" facing from the given positions,
     * so that pos1 is in the "front left" corner and pos2 is in the "back right" corner
     * of the area, when looking at the "front" face of the area.
     */
    public static Direction getFacingFromPositions(BlockPos pos1, BlockPos pos2) {
        if (pos1 == null || pos2 == null) {
            return null;
        }

        return getFacingFromPositions(pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ());
    }

    private static Direction getFacingFromPositions(int x1, int z1, int x2, int z2) {
        if (x2 == x1) {
            return z2 > z1 ? Direction.SOUTH : Direction.NORTH;
        }

        if (z2 == z1) {
            return x2 > x1 ? Direction.EAST : Direction.WEST;
        }

        if (x2 > x1) {
            return z2 > z1 ? Direction.EAST : Direction.NORTH;
        }

        return z2 > z1 ? Direction.SOUTH : Direction.WEST;
    }

    public static Rotation cycleRotation(Rotation rotation, boolean reverse) {
        int ordinal = rotation.ordinal();

        if (reverse) {
            ordinal = ordinal == 0 ? Rotation.values().length - 1 : ordinal - 1;
        } else {
            ordinal = ordinal >= Rotation.values().length - 1 ? 0 : ordinal + 1;
        }

        return Rotation.values()[ordinal];
    }

    public static Mirror cycleMirror(Mirror mirror, boolean reverse) {
        int ordinal = mirror.ordinal();

        if (reverse) {
            ordinal = ordinal == 0 ? Mirror.values().length - 1 : ordinal - 1;
        } else {
            ordinal = ordinal >= Mirror.values().length - 1 ? 0 : ordinal + 1;
        }

        return Mirror.values()[ordinal];
    }

    public static String getRotationNameShort(Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_90:
                return "CW_90";
            case CLOCKWISE_180:
                return "CW_180";
            case COUNTERCLOCKWISE_90:
                return "CCW_90";
            case NONE:
            default:
                return "NONE";
        }
    }

    public static String getMirrorName(Mirror mirror) {
        switch (mirror) {
            case FRONT_BACK:
                return "FRONT_BACK";
            case LEFT_RIGHT:
                return "LEFT_RIGHT";
            case NONE:
            default:
                return "NONE";
        }
    }

    private static float wrapDegrees(float degrees) {
        float f = degrees % 360.0F;
        if (f >= 180.0F) {
            f -= 360.0F;
        }

        if (f < -180.0F) {
            f += 360.0F;
        }

        return f;
    }

    public static float getRotatedYaw(float yaw, Rotation rotation) {
        yaw = wrapDegrees(yaw);

        switch (rotation) {
            case CLOCKWISE_180:
                yaw += 180.0F;
                break;
            case COUNTERCLOCKWISE_90:
                yaw += 270.0F;
                break;
            case CLOCKWISE_90:
                yaw += 90.0F;
                break;
            default:
        }

        return yaw;
    }

    public static float getMirroredYaw(float yaw, Mirror mirror) {
        yaw = wrapDegrees(yaw);

        switch (mirror) {
            case LEFT_RIGHT:
                yaw = 180.0F - yaw;
                break;
            case FRONT_BACK:
                yaw = -yaw;
                break;
            default:
        }

        return yaw;
    }

    public static class BlockPosComparator implements Comparator<BlockPos> {
        private BlockPos posReference = BlockPos.ZERO;
        private boolean closestFirst;

        public void setClosestFirst(boolean closestFirst) {
            this.closestFirst = closestFirst;
        }

        public void setReferencePosition(BlockPos pos) {
            this.posReference = pos;
        }

        @Override
        public int compare(BlockPos pos1, BlockPos pos2) {
            double dist1 = pos1.distSqr(this.posReference);
            double dist2 = pos2.distSqr(this.posReference);

            if (dist1 == dist2) {
                return 0;
            }

            return dist1 < dist2 == this.closestFirst ? -1 : 1;
        }
    }

    public static class ChunkPosComparator implements Comparator<ChunkPos> {
        private BlockPos posReference = BlockPos.ZERO;
        private boolean closestFirst;

        public ChunkPosComparator setClosestFirst(boolean closestFirst) {
            this.closestFirst = closestFirst;
            return this;
        }

        public ChunkPosComparator setReferencePosition(BlockPos pos) {
            this.posReference = pos;
            return this;
        }

        @Override
        public int compare(ChunkPos pos1, ChunkPos pos2) {
            double dist1 = this.distanceSq(pos1);
            double dist2 = this.distanceSq(pos2);

            if (dist1 == dist2) {
                return 0;
            }

            return dist1 < dist2 == this.closestFirst ? -1 : 1;
        }

        private double distanceSq(ChunkPos pos) {
            double dx = (double) (pos.x << 4) - this.posReference.getX();
            double dz = (double) (pos.z << 4) - this.posReference.getZ();

            return dx * dx + dz * dz;
        }
    }

    public static class ChunkPosDistanceComparator implements Comparator<ChunkPos> {
        private final ChunkPos referencePosition;

        public ChunkPosDistanceComparator(ChunkPos referencePosition) {
            this.referencePosition = referencePosition;
        }

        @Override
        public int compare(ChunkPos pos1, ChunkPos pos2) {
            int refX = this.referencePosition.x;
            int refZ = this.referencePosition.z;

            double dist1 = (refX - pos1.x) * (refX - pos1.x) + (refZ - pos1.z) * (refZ - pos1.z);
            double dist2 = (refX - pos2.x) * (refX - pos2.x) + (refZ - pos2.z) * (refZ - pos2.z);

            return Double.compare(dist1, dist2);
        }
    }

    public enum Corner {
        NONE,
        CORNER_1,
        CORNER_2
    }
}