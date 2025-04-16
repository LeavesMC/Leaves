package org.leavesmc.leaves.protocol.servux.litematics.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import org.leavesmc.leaves.protocol.servux.ServuxProtocol;
import org.leavesmc.leaves.protocol.servux.litematics.LitematicaSchematic;
import org.leavesmc.leaves.protocol.servux.litematics.selection.Box;
import org.leavesmc.leaves.protocol.servux.litematics.utils.IntBoundingBox;
import org.leavesmc.leaves.protocol.servux.litematics.utils.PositionUtils;
import org.leavesmc.leaves.protocol.servux.litematics.utils.ReplaceBehavior;
import org.leavesmc.leaves.protocol.servux.litematics.utils.SchematicPlacingUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class SchematicPlacement {

    private final Map<String, SubRegionPlacement> relativeSubRegionPlacements = new HashMap<>();
    private final LitematicaSchematic schematic;
    private BlockPos origin;
    private String name;
    private Rotation rotation = Rotation.NONE;
    private Mirror mirror = Mirror.NONE;

    private SchematicPlacement(LitematicaSchematic schematic, BlockPos origin, String name) {
        this.schematic = schematic;
        this.origin = origin;
        this.name = name;
    }

    public static SchematicPlacement createFromNbt(CompoundTag tags) {
        try {
            SchematicPlacement placement = new SchematicPlacement(new LitematicaSchematic(tags.getCompound("Schematics")), NbtUtils.readBlockPos(tags, "Origin").orElseThrow(), tags.getString("Name"));
            placement.mirror = Mirror.values()[tags.getInt("Mirror")];
            placement.rotation = Rotation.values()[tags.getInt("Rotation")];
            for (String name : tags.getCompound("SubRegions").getAllKeys()) {
                CompoundTag compound = tags.getCompound("SubRegions").getCompound(name);
                var sub = new SubRegionPlacement(NbtUtils.readBlockPos(compound, "Pos").orElseThrow(), compound.getString("Name"));
                sub.mirror = Mirror.values()[compound.getInt("Mirror")];
                sub.rotation = Rotation.values()[compound.getInt("Rotation")];
                sub.ignoreEntities = compound.getBoolean("IgnoreEntities");
                sub.enabled = compound.getBoolean("Enabled");
                placement.relativeSubRegionPlacements.put(name, sub);
            }
            return placement;
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean shouldRenderEnclosingBox() {
        return false;
    }

    public boolean ignoreEntities() {
        return false;
    }

    public String getName() {
        return this.name;
    }

    public LitematicaSchematic getSchematic() {
        return schematic;
    }

    public void setName(String name) {
        this.name = name;
    }


    public BlockPos getOrigin() {
        return origin;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public Mirror getMirror() {
        return mirror;
    }

    @Nullable
    public SubRegionPlacement getRelativeSubRegionPlacement(String areaName) {
        return this.relativeSubRegionPlacements.get(areaName);
    }

    private void updateEnclosingBox() {
        if (!this.shouldRenderEnclosingBox()) {
            return;
        }
        ImmutableMap<String, Box> boxes = this.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.ANY);
        BlockPos pos1 = null;
        BlockPos pos2 = null;

        for (Box box : boxes.values()) {
            BlockPos boxPos1 = box.getPos1();
            BlockPos boxPos2 = box.getPos2();
            if (boxPos1 == null || boxPos2 == null) continue;
            BlockPos tmp;
            tmp = BlockPos.min(boxPos1, boxPos2);

            if (pos1 == null) {
                pos1 = tmp;
            } else if (tmp.getX() < pos1.getX() || tmp.getY() < pos1.getY() || tmp.getZ() < pos1.getZ()) {
                pos1 = BlockPos.min(tmp, pos1);
            }

            tmp = BlockPos.max(boxPos1, boxPos2);

            if (pos2 == null) {
                pos2 = tmp;
            } else if (tmp.getX() > pos2.getX() || tmp.getY() > pos2.getY() || tmp.getZ() > pos2.getZ()) {
                pos2 = BlockPos.max(tmp, pos2);
            }
        }
    }

    public ImmutableMap<String, Box> getSubRegionBoxes(SubRegionPlacement.RequiredEnabled required) {
        ImmutableMap.Builder<String, Box> builder = ImmutableMap.builder();
        Map<String, BlockPos> areaSizes = this.schematic.getAreaSizes();

        for (Map.Entry<String, SubRegionPlacement> entry : this.relativeSubRegionPlacements.entrySet()) {
            String name = entry.getKey();
            BlockPos areaSize = areaSizes.get(name);

            if (areaSize == null) {
                ServuxProtocol.LOGGER.warn("SchematicPlacement.getSubRegionBoxes(): Size for sub-region '{}' not found in the schematic '{}'", name, this.schematic.getMetadata().getName());
                continue;
            }

            SubRegionPlacement placement = entry.getValue();

            if (placement.matchesRequirement(required)) {
                putBoxPosIntoBuilder(builder, name, areaSize, placement);
            }
        }

        return builder.build();
    }

    private void putBoxPosIntoBuilder(ImmutableMap.Builder<String, Box> builder, String name, BlockPos areaSize, SubRegionPlacement placement) {
        BlockPos boxOriginRelative = placement.getPos();

        BlockPos boxOriginAbsolute = PositionUtils.getTransformedBlockPos(boxOriginRelative, this.mirror, this.rotation).offset(this.origin);
        BlockPos pos2 = PositionUtils.getRelativeEndPositionFromAreaSize(areaSize);
        pos2 = PositionUtils.getTransformedBlockPos(pos2, this.mirror, this.rotation);
        pos2 = PositionUtils.getTransformedBlockPos(pos2, placement.getMirror(), placement.getRotation()).offset(boxOriginAbsolute);

        builder.put(name, new Box(boxOriginAbsolute, pos2, name));
    }

    public static IntBoundingBox getBoundsWithinChunkForBox(Box box, int chunkX, int chunkZ) {
        final int chunkXMin = chunkX << 4;
        final int chunkZMin = chunkZ << 4;
        final int chunkXMax = chunkXMin + 15;
        final int chunkZMax = chunkZMin + 15;
        BlockPos boxPos1 = box.getPos1();
        BlockPos boxPos2 = box.getPos2();
        if (boxPos1 == null || boxPos2 == null) return null;
        int x1 = boxPos1.getX();
        int x2 = boxPos2.getX();
        int y1 = boxPos1.getY();
        int y2 = boxPos2.getY();
        int z1 = boxPos1.getZ();
        int z2 = boxPos2.getZ();
        final int boxXMin = Math.min(x1, x2);
        final int boxZMin = Math.min(z1, z2);
        final int boxXMax = Math.max(x1, x2);
        final int boxZMax = Math.max(z1, z2);

        boolean notOverlapping = boxXMin > chunkXMax || boxZMin > chunkZMax || boxXMax < chunkXMin || boxZMax < chunkZMin;

        if (!notOverlapping) {
            final int xMin = Math.max(chunkXMin, boxXMin);
            final int yMin = Math.min(y1, y2);
            final int zMin = Math.max(chunkZMin, boxZMin);
            final int xMax = Math.min(chunkXMax, boxXMax);
            final int yMax = Math.max(y1, y2);
            final int zMax = Math.min(chunkZMax, boxZMax);

            return new IntBoundingBox(xMin, yMin, zMin, xMax, yMax, zMax);
        }

        return null;
    }

    public ImmutableMap<String, Box> getSubRegionBoxFor(String regionName, SubRegionPlacement.RequiredEnabled required) {
        ImmutableMap.Builder<String, Box> builder = ImmutableMap.builder();
        Map<String, BlockPos> areaSizes = this.schematic.getAreaSizes();

        SubRegionPlacement placement = this.relativeSubRegionPlacements.get(regionName);

        if (placement != null) {
            if (placement.matchesRequirement(required)) {
                BlockPos areaSize = areaSizes.get(regionName);

                if (areaSize != null) {
                    putBoxPosIntoBuilder(builder, regionName, areaSize, placement);
                } else {
                    ServuxProtocol.LOGGER.warn("SchematicPlacement.getSubRegionBoxFor(): Size for sub-region '{}' not found in the schematic '{}'", regionName, this.schematic.getMetadata().getName());
                }
            }
        }

        return builder.build();
    }

    public Set<String> getRegionsTouchingChunk(int chunkX, int chunkZ) {
        ImmutableMap<String, Box> map = this.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.PLACEMENT_ENABLED);
        final int chunkXMin = chunkX << 4;
        final int chunkZMin = chunkZ << 4;
        final int chunkXMax = chunkXMin + 15;
        final int chunkZMax = chunkZMin + 15;
        Set<String> set = new HashSet<>();

        for (Map.Entry<String, Box> entry : map.entrySet()) {
            Box box = entry.getValue();
            BlockPos boxPos1 = box.getPos1();
            BlockPos boxPos2 = box.getPos2();
            if (boxPos1 == null || boxPos2 == null) continue;
            int x1 = boxPos1.getX();
            int x2 = boxPos2.getX();
            int z1 = boxPos1.getZ();
            int z2 = boxPos2.getZ();
            final int boxXMin = Math.min(x1, x2);
            final int boxZMin = Math.min(z1, z2);
            final int boxXMax = Math.max(x1, x2);
            final int boxZMax = Math.max(z1, z2);

            boolean notOverlapping = boxXMin > chunkXMax || boxZMin > chunkZMax || boxXMax < chunkXMin || boxZMax < chunkZMin;

            if (!notOverlapping) {
                set.add(entry.getKey());
            }
        }

        return set;
    }

    @Nullable
    public IntBoundingBox getBoxWithinChunkForRegion(String regionName, int chunkX, int chunkZ) {
        Box box = this.getSubRegionBoxFor(regionName, SubRegionPlacement.RequiredEnabled.PLACEMENT_ENABLED).get(regionName);
        return box != null ? getBoundsWithinChunkForBox(box, chunkX, chunkZ) : null;
    }

    public enum CoordinateType {
        X,
        Y,
        Z
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

    public SchematicPlacement setOrigin(BlockPos origin) {
        origin = getModifiedPartiallyLockedPosition(this.origin, origin, 0);

        if (!this.origin.equals(origin)) {
            // Marks the currently touched chunks before doing the modification

            this.origin = origin;
            this.updateEnclosingBox();
        }

        return this;
    }

    public SchematicPlacement setRotation(Rotation rotation) {

        if (this.rotation != rotation) {
            // Marks the currently touched chunks before doing the modification


            this.rotation = rotation;
            this.updateEnclosingBox();
        }

        return this;
    }

    public SchematicPlacement setMirror(Mirror mirror) {
        if (this.mirror != mirror) {
            // Marks the currently touched chunks before doing the modification

            this.mirror = mirror;
            this.updateEnclosingBox();
        }

        return this;
    }

    private Box getEnclosingBox() {
        ImmutableMap<String, Box> boxes = this.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.ANY);
        BlockPos pos1 = null;
        BlockPos pos2 = null;

        for (Box box : boxes.values()) {
            BlockPos tmp;
            BlockPos boxPos1 = box.getPos1();
            BlockPos boxPos2 = box.getPos2();
            if (boxPos1 == null || boxPos2 == null) continue;
            tmp = PositionUtils.getMinCorner(boxPos1, boxPos2);

            if (pos1 == null) {
                pos1 = tmp;
            } else if (tmp.getX() < pos1.getX() || tmp.getY() < pos1.getY() || tmp.getZ() < pos1.getZ()) {
                pos1 = PositionUtils.getMinCorner(tmp, pos1);
            }

            tmp = PositionUtils.getMaxCorner(boxPos1, boxPos2);

            if (pos2 == null) {
                pos2 = tmp;
            } else if (tmp.getX() > pos2.getX() || tmp.getY() > pos2.getY() || tmp.getZ() > pos2.getZ()) {
                pos2 = PositionUtils.getMaxCorner(tmp, pos2);
            }
        }

        if (pos1 != null) {
            return new Box(pos1, pos2, "Enclosing Box (Servux)");
        }

        return null;
    }

    public Stream<ChunkPos> streamChunkPos(BlockBox box) {
        AABB aabb = box.aabb();
        int i = SectionPos.blockToSectionCoord(aabb.minX);
        int j = SectionPos.blockToSectionCoord(aabb.minZ);
        int k = SectionPos.blockToSectionCoord(aabb.maxX);
        int l = SectionPos.blockToSectionCoord(aabb.maxZ);
        return ChunkPos.rangeClosed(new ChunkPos(i, j), new ChunkPos(k, l));
    }

    public void pasteTo(ServerLevel serverWorld, ReplaceBehavior replaceBehavior) {
        Box enclosingBox = this.getEnclosingBox();
        if (enclosingBox == null) {
            ServuxProtocol.LOGGER.error("receiver a null enclosing box");
            return;
        }
        streamChunkPos(enclosingBox.toVanilla()).forEach(chunkPos -> SchematicPlacingUtils.placeToWorldWithinChunk(serverWorld, chunkPos, this, replaceBehavior, false));
    }
}
