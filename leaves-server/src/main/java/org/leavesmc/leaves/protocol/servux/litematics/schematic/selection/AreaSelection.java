package org.leavesmc.leaves.protocol.servux.litematics.schematic.selection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.utils.JsonUtils;
import org.leavesmc.leaves.protocol.servux.litematics.schematic.utils.PositionUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class AreaSelection {
    protected final Map<String, Box> subRegionBoxes = new HashMap<>();
    protected String name = "Unnamed";
    protected boolean originSelected;
    protected BlockPos calculatedOrigin = BlockPos.ZERO;
    protected boolean calculatedOriginDirty = true;
    @Nullable
    protected BlockPos explicitOrigin = null;
    @Nullable
    protected String currentBox;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
    /**
     * Get the explicitly defined origin point, if any.
     *
     * @return
     */
    @Nullable
    public BlockPos getExplicitOrigin() {
        return this.explicitOrigin;
    }

    public void setExplicitOrigin(@Nullable BlockPos origin) {
        this.explicitOrigin = origin;

        if (origin == null) {
            this.originSelected = false;
        }
    }

    protected void updateCalculatedOrigin() {
        Pair<BlockPos, BlockPos> pair = PositionUtils.getEnclosingAreaCorners(this.subRegionBoxes.values());

        if (pair != null) {
            this.calculatedOrigin = pair.getLeft();
        } else {
            this.calculatedOrigin = BlockPos.ZERO;
        }

        this.calculatedOriginDirty = false;
    }
    public AreaSelection copy() {
        return fromJson(this.toJson());
    }

    public static AreaSelection fromJson(JsonObject obj) {
        AreaSelection area = new AreaSelection();

        if (JsonUtils.hasArray(obj, "boxes")) {
            JsonArray arr = obj.get("boxes").getAsJsonArray();
            final int size = arr.size();

            for (int i = 0; i < size; i++) {
                JsonElement el = arr.get(i);

                if (el.isJsonObject()) {
                    Box box = Box.fromJson(el.getAsJsonObject());

                    if (box != null) {
                        area.subRegionBoxes.put(box.getName(), box);
                    }
                }
            }
        }

        if (JsonUtils.hasString(obj, "name")) {
            area.name = obj.get("name").getAsString();
        }

        if (JsonUtils.hasString(obj, "current")) {
            area.currentBox = obj.get("current").getAsString();
        }

        BlockPos pos = JsonUtils.blockPosFromJson(obj, "origin");

        if (pos != null) {
            area.setExplicitOrigin(pos);
        } else {
            area.updateCalculatedOrigin();
        }

        return area;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();

        for (Box box : this.subRegionBoxes.values()) {
            JsonObject o = box.toJson();

            if (o != null) {
                arr.add(o);
            }
        }

        obj.add("name", new JsonPrimitive(this.name));

        if (!arr.isEmpty()) {
            if (this.currentBox != null) {
                obj.add("current", new JsonPrimitive(this.currentBox));
            }

            obj.add("boxes", arr);
        }

        if (this.getExplicitOrigin() != null) {
            obj.add("origin", JsonUtils.blockPosToJson(this.getExplicitOrigin()));
        }

        return obj;
    }
}
