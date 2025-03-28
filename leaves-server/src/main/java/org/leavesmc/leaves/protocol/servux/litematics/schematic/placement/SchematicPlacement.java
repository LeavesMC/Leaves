package org.leavesmc.leaves.protocol.servux.litematics.schematic.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.leavesmc.leaves.protocol.servux.litematics.ServuxLitematicsProtocol;
import org.leavesmc.leaves.protocol.servux.litematics.malilib.IntBoundingBox;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.LitematicaSchematic;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.selection.Box;

import javax.annotation.Nullable;
import java.util.*;

public class SchematicPlacement {
    private static final Set<Integer> USED_COLORS = new HashSet<>();

    private final Map<String, SubRegionPlacement> relativeSubRegionPlacements = new HashMap<>();
    private final int subRegionCount;
    private final LitematicaSchematic schematic;
    private BlockPos origin;
    private String name;
    private Rotation rotation = Rotation.NONE;
    private Mirror mirror = Mirror.NONE;
    private boolean ignoreEntities;
    private boolean regionPlacementsModified;
    private int coordinateLockMask;
    @Nullable
    private Box enclosingBox;

    private SchematicPlacement(LitematicaSchematic schematic, BlockPos origin, String name, boolean ignoreEntities) {
        this.schematic = schematic;
        this.origin = origin;
        this.name = name;
        this.subRegionCount = schematic.getSubRegionCount();
        this.ignoreEntities = ignoreEntities;
    }

    public static SchematicPlacement createFor(LitematicaSchematic schematic, BlockPos origin, String name, boolean ignoreEntities) {
        SchematicPlacement placement = new SchematicPlacement(schematic, origin, name, ignoreEntities);
        placement.resetAllSubRegionsToSchematicValues();

        return placement;
    }

    public static SchematicPlacement createFromNbt(CompoundTag tags) {
        try {
            SchematicPlacement placement = new SchematicPlacement(new LitematicaSchematic(tags.getCompound("Schematics")), NbtUtils.readBlockPos(tags, "Origin").orElseThrow(), tags.getString("Name"), false);
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

    public boolean isRegionPlacementModified() {
        return this.regionPlacementsModified;
    }

    public boolean ignoreEntities() {
        return this.ignoreEntities;
    }

    public String getName() {
        return this.name;
    }

    public LitematicaSchematic getSchematic() {
        return schematic;
    }

    @Nullable
    public Box getEclosingBox() {
        return this.enclosingBox;
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


    public int getSubRegionCount() {
        return this.subRegionCount;
    }

    @Nullable
    public SubRegionPlacement getRelativeSubRegionPlacement(String areaName) {
        return this.relativeSubRegionPlacements.get(areaName);
    }

    public Collection<SubRegionPlacement> getAllSubRegionsPlacements() {
        return this.relativeSubRegionPlacements.values();
    }

    public ImmutableMap<String, SubRegionPlacement> getEnabledRelativeSubRegionPlacements() {
        ImmutableMap.Builder<String, SubRegionPlacement> builder = ImmutableMap.builder();

        for (Map.Entry<String, SubRegionPlacement> entry : this.relativeSubRegionPlacements.entrySet()) {
            SubRegionPlacement placement = entry.getValue();

            if (placement.matchesRequirement(SubRegionPlacement.RequiredEnabled.PLACEMENT_ENABLED)) {
                builder.put(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    /*
    public ImmutableMap<String, Box> getAllSubRegionBoxes()
    {
        return this.getSubRegionBoxes(RequiredEnabled.ANY);
    }
    */

    private void updateEnclosingBox() {
        if (this.shouldRenderEnclosingBox()) {
            ImmutableMap<String, Box> boxes = this.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.ANY);
            BlockPos pos1 = null;
            BlockPos pos2 = null;

            for (Box box : boxes.values()) {
                BlockPos tmp;
                tmp = BlockPos.min(box.getPos1(), box.getPos2());

                if (pos1 == null) {
                    pos1 = tmp;
                } else if (tmp.getX() < pos1.getX() || tmp.getY() < pos1.getY() || tmp.getZ() < pos1.getZ()) {
                    pos1 = BlockPos.min(tmp, pos1);
                }

                tmp = BlockPos.max(box.getPos1(), box.getPos2());

                if (pos2 == null) {
                    pos2 = tmp;
                } else if (tmp.getX() > pos2.getX() || tmp.getY() > pos2.getY() || tmp.getZ() > pos2.getZ()) {
                    pos2 = BlockPos.max(tmp, pos2);
                }
            }

            if (pos1 != null && pos2 != null) {
                this.enclosingBox = new Box(pos1, pos2, "Enclosing Box");
            }
        }
    }


    private static BlockPos getRelativeEndPositionFromAreaSize(BlockPos size) {
        int x = size.getX();
        int y = size.getY();
        int z = size.getZ();

        x = x >= 0 ? x - 1 : x + 1;
        y = y >= 0 ? y - 1 : y + 1;
        z = z >= 0 ? z - 1 : z + 1;

        return new BlockPos(x, y, z);
    }

    public ImmutableMap<String, Box> getSubRegionBoxes(SubRegionPlacement.RequiredEnabled required) {
        ImmutableMap.Builder<String, Box> builder = ImmutableMap.builder();
        Map<String, BlockPos> areaSizes = this.schematic.getAreaSizes();

        for (Map.Entry<String, SubRegionPlacement> entry : this.relativeSubRegionPlacements.entrySet()) {
            String name = entry.getKey();
            BlockPos areaSize = areaSizes.get(name);

            if (areaSize == null) {
                ServuxLitematicsProtocol.LOGGER.warn("SchematicPlacement.getSubRegionBoxes(): Size for sub-region '{}' not found in the schematic '{}'", name, this.schematic.getMetadata().getName());
                continue;
            }

            SubRegionPlacement placement = entry.getValue();

            if (placement.matchesRequirement(required)) {
                BlockPos boxOriginRelative = placement.getPos();

                BlockPos boxOriginAbsolute = StructureTemplate.transform(boxOriginRelative, this.mirror, this.rotation, this.origin);
                BlockPos pos2 = getRelativeEndPositionFromAreaSize(areaSize);
                pos2 = StructureTemplate.transform(pos2, this.mirror, this.rotation, BlockPos.ZERO);
                pos2 = StructureTemplate.transform(pos2, placement.getMirror(), placement.getRotation(), boxOriginAbsolute);

                builder.put(name, new Box(boxOriginAbsolute, pos2, name));
            }
        }

        return builder.build();
    }

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

        if (notOverlapping == false) {
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

    public ImmutableMap<String, Box> getSubRegionBoxFor(String regionName, SubRegionPlacement.RequiredEnabled required) {
        ImmutableMap.Builder<String, Box> builder = ImmutableMap.builder();
        Map<String, BlockPos> areaSizes = this.schematic.getAreaSizes();

        SubRegionPlacement placement = this.relativeSubRegionPlacements.get(regionName);

        if (placement != null) {
            if (placement.matchesRequirement(required)) {
                BlockPos areaSize = areaSizes.get(regionName);

                if (areaSize != null) {
                    BlockPos boxOriginRelative = placement.getPos();
                    BlockPos boxOriginAbsolute = StructureTemplate.transform(boxOriginRelative, this.mirror, this.rotation, this.origin);
                    BlockPos pos2 = getRelativeEndPositionFromAreaSize(areaSize);
                    pos2 = StructureTemplate.transform(pos2, this.mirror, this.rotation, BlockPos.ZERO);
                    pos2 = StructureTemplate.transform(pos2, placement.getMirror(), placement.getRotation(), boxOriginAbsolute);

                    builder.put(regionName, new Box(boxOriginAbsolute, pos2, regionName));
                } else {
                    ServuxLitematicsProtocol.LOGGER.warn("SchematicPlacement.getSubRegionBoxFor(): Size for sub-region '{}' not found in the schematic '{}'", regionName, this.schematic.getMetadata().getName());
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
            final int boxXMin = Math.min(box.getPos1().getX(), box.getPos2().getX());
            final int boxZMin = Math.min(box.getPos1().getZ(), box.getPos2().getZ());
            final int boxXMax = Math.max(box.getPos1().getX(), box.getPos2().getX());
            final int boxZMax = Math.max(box.getPos1().getZ(), box.getPos2().getZ());

            boolean notOverlapping = boxXMin > chunkXMax || boxZMin > chunkZMax || boxXMax < chunkXMin || boxZMax < chunkZMin;

            if (notOverlapping == false) {
                set.add(entry.getKey());
            }
        }

        return set;
    }

    public ImmutableMap<String, IntBoundingBox> getBoxesWithinChunk(int chunkX, int chunkZ) {
        ImmutableMap<String, Box> subRegions = this.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.PLACEMENT_ENABLED);
        return getBoxesWithinChunk(chunkX, chunkZ, subRegions);
    }

    @Nullable
    public IntBoundingBox getBoxWithinChunkForRegion(String regionName, int chunkX, int chunkZ) {
        Box box = this.getSubRegionBoxFor(regionName, SubRegionPlacement.RequiredEnabled.PLACEMENT_ENABLED).get(regionName);
        return box != null ? getBoundsWithinChunkForBox(box, chunkX, chunkZ) : null;
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

    public Set<ChunkPos> getTouchedChunks() {
        return getTouchedChunks(this.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.PLACEMENT_ENABLED));
    }

    public Set<ChunkPos> getTouchedChunksForRegion(String regionName) {
        return getTouchedChunks(this.getSubRegionBoxFor(regionName, SubRegionPlacement.RequiredEnabled.PLACEMENT_ENABLED));
    }

    private void checkAreSubRegionsModified() {
        Map<String, BlockPos> areaPositions = this.schematic.getAreaPositions();

        if (areaPositions.size() != this.relativeSubRegionPlacements.size()) {
            this.regionPlacementsModified = true;
            return;
        }

        for (Map.Entry<String, BlockPos> entry : areaPositions.entrySet()) {
            SubRegionPlacement placement = this.relativeSubRegionPlacements.get(entry.getKey());

            if (placement == null || placement.isRegionPlacementModified(entry.getValue())) {
                this.regionPlacementsModified = true;
                return;
            }
        }

        this.regionPlacementsModified = false;
    }

    /**
     * Moves the sub-region to the given <b>absolute</b> position.
     *
     * @param regionName
     * @param newPos
     */
    public void moveSubRegionTo(String regionName, BlockPos newPos) {
        if (this.relativeSubRegionPlacements.containsKey(regionName)) {
            // Marks the currently touched chunks before doing the modification


            // The input argument position is an absolute position, so need to convert to relative position here
            newPos = newPos.subtract(this.origin);
            // The absolute-based input position needs to be transformed if the entire placement has been rotated or mirrored
            newPos = StructureTemplate.transform(newPos, this.mirror, this.rotation, BlockPos.ZERO);

            this.relativeSubRegionPlacements.get(regionName).setPos(newPos);
            this.onModified();
        }
    }

    public void setSubRegionRotation(String regionName, Rotation rotation) {


        if (this.relativeSubRegionPlacements.containsKey(regionName)) {
            // Marks the currently touched chunks before doing the modification


            this.relativeSubRegionPlacements.get(regionName).setRotation(rotation);
            this.onModified();
        }
    }

    public void setSubRegionMirror(String regionName, Mirror mirror) {

        if (this.relativeSubRegionPlacements.containsKey(regionName)) {
            // Marks the currently touched chunks before doing the modification


            this.relativeSubRegionPlacements.get(regionName).setMirror(mirror);
            this.onModified();
        }
    }


    public void resetAllSubRegionsToSchematicValues() {
        this.resetAllSubRegionsToSchematicValues(true);
    }

    public void resetAllSubRegionsToSchematicValues(boolean updatePlacementManager) {

        if (updatePlacementManager) {
            // Marks the currently touched chunks before doing the modification

        }

        Map<String, BlockPos> areaPositions = this.schematic.getAreaPositions();
        this.relativeSubRegionPlacements.clear();
        this.regionPlacementsModified = false;

        for (Map.Entry<String, BlockPos> entry : areaPositions.entrySet()) {
            String name = entry.getKey();
            this.relativeSubRegionPlacements.put(name, new SubRegionPlacement(entry.getValue(), name));
        }

        if (updatePlacementManager) {
            this.updateEnclosingBox();
        }
    }

    public void resetSubRegionToSchematicValues(String regionName) {

        BlockPos pos = this.schematic.getSubRegionPosition(regionName);
        SubRegionPlacement placement = this.relativeSubRegionPlacements.get(regionName);

        if (pos != null && placement != null) {
            // Marks the currently touched chunks before doing the modification


            placement.resetToOriginalValues();
            this.onModified();
        }
    }

    public enum CoordinateType {
        X,
        Y,
        Z;

        private CoordinateType() {
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

    public SchematicPlacement setOrigin(BlockPos origin) {


        origin = getModifiedPartiallyLockedPosition(this.origin, origin, this.coordinateLockMask);

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

    private void onModified() {
        this.checkAreSubRegionsModified();
        this.updateEnclosingBox();
    }

    public void onRemoved() {
        if (USED_COLORS.isEmpty()) {
        }
    }

    private Box getEnclosingBox() {
        ImmutableMap<String, Box> boxes = this.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.ANY);
        BlockPos pos1 = null;
        BlockPos pos2 = null;

        for (Box box : boxes.values()) {
            BlockPos tmp;
            tmp = BlockPos.min(box.getPos1(), box.getPos2());

            if (pos1 == null) {
                pos1 = tmp;
            } else if (tmp.getX() < pos1.getX() || tmp.getY() < pos1.getY() || tmp.getZ() < pos1.getZ()) {
                pos1 = BlockPos.min(tmp, pos1);
            }

            tmp = BlockPos.max(box.getPos1(), box.getPos2());

            if (pos2 == null) {
                pos2 = tmp;
            } else if (tmp.getX() > pos2.getX() || tmp.getY() > pos2.getY() || tmp.getZ() > pos2.getZ()) {
                pos2 = BlockPos.max(tmp, pos2);
            }
        }

        if (pos1 != null && pos2 != null) {
            return new Box(pos1, pos2, "Enclosing Box (Servux)");
        }

        return null;
    }

    public void pasteTo(ServerLevel serverWorld, ReplaceBehavior replaceBehavior) {
        this.getEnclosingBox().toVanilla().streamChunkPos().forEach(chunkPos ->
            SchematicPlacingUtils.placeToWorldWithinChunk(serverWorld, chunkPos, this, replaceBehavior, false));
        // todo
    }
}
