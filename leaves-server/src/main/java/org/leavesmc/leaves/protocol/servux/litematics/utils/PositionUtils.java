package org.leavesmc.leaves.protocol.servux.litematics.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import org.leavesmc.leaves.protocol.servux.litematics.placement.SchematicPlacement;
import org.leavesmc.leaves.protocol.servux.litematics.placement.SubRegionPlacement;

public class PositionUtils {

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

    public static BlockPos getMinCorner(BlockPos pos1, BlockPos pos2) {
        return new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
    }

    public static BlockPos getMaxCorner(BlockPos pos1, BlockPos pos2) {
        return new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
    }

    public static BlockPos getTransformedPlacementPosition(BlockPos posWithinSub, SchematicPlacement schematicPlacement, SubRegionPlacement placement) {
        BlockPos pos = posWithinSub;
        pos = getTransformedBlockPos(pos, schematicPlacement.getMirror(), schematicPlacement.getRotation());
        pos = getTransformedBlockPos(pos, placement.mirror(), placement.rotation());
        return pos;
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

    /**
     * Mirrors and then rotates the given position around the origin
     */
    public static BlockPos getTransformedBlockPos(BlockPos pos, Mirror mirror, Rotation rotation) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean isMirrored = true;

        switch (mirror) {
            case LEFT_RIGHT -> z = -z; // LEFT_RIGHT is essentially NORTH_SOUTH
            case FRONT_BACK -> x = -x; // FRONT_BACK is essentially EAST_WEST
            default -> isMirrored = false;
        }

        return switch (rotation) {
            case CLOCKWISE_90 -> new BlockPos(-z, y, x);
            case COUNTERCLOCKWISE_90 -> new BlockPos(z, y, -x);
            case CLOCKWISE_180 -> new BlockPos(-x, y, -z);
            default -> isMirrored ? new BlockPos(x, y, z) : pos;
        };
    }

    public static BlockPos getReverseTransformedBlockPos(BlockPos pos, Mirror mirror, Rotation rotation) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean isRotated = true;
        int tmp = x;

        switch (rotation) {
            case CLOCKWISE_90 -> {
                x = z;
                z = -tmp;
            }
            case COUNTERCLOCKWISE_90 -> {
                x = -z;
                z = tmp;
            }
            case CLOCKWISE_180 -> {
                x = -x;
                z = -z;
            }
            default -> isRotated = false;
        }

        switch (mirror) {
            case LEFT_RIGHT -> z = -z; // LEFT_RIGHT is essentially NORTH_SOUTH
            case FRONT_BACK -> x = -x; // FRONT_BACK is essentially EAST_WEST
            default -> {
                if (!isRotated) {
                    return pos;
                }
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
            case LEFT_RIGHT -> z = 1.0D - z;
            case FRONT_BACK -> x = 1.0D - x;
            default -> transformed = false;
        }

        return switch (rotation) {
            case COUNTERCLOCKWISE_90 -> new Vec3(z, y, 1.0D - x);
            case CLOCKWISE_90 -> new Vec3(1.0D - z, y, x);
            case CLOCKWISE_180 -> new Vec3(1.0D - x, y, 1.0D - z);
            default -> transformed ? new Vec3(x, y, z) : originalPos;
        };
    }
}