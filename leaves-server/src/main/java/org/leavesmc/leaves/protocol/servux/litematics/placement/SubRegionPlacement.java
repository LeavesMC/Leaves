package org.leavesmc.leaves.protocol.servux.litematics.placement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.leavesmc.leaves.protocol.servux.ServuxProtocol;

public class SubRegionPlacement {

    private final String name;
    private final BlockPos pos;
    public Rotation rotation = Rotation.NONE;
    public Mirror mirror = Mirror.NONE;
    public boolean enabled = true;
    public boolean ignoreEntities;

    public SubRegionPlacement(BlockPos pos, String name) {
        this.pos = pos;
        this.name = name;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean ignoreEntities() {
        return this.ignoreEntities;
    }

    public boolean matchesRequirement(RequiredEnabled required) {
        if (required == RequiredEnabled.ANY) {
            return true;
        }

        if (required == RequiredEnabled.PLACEMENT_ENABLED) {
            return this.isEnabled();
        }

        ServuxProtocol.LOGGER.warn("RequiredEnabled.RENDERING_ENABLED is not supported on server side!");
        return false;
    }

    public String getName() {
        return this.name;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();

        arr.add(this.pos.getX());
        arr.add(this.pos.getY());
        arr.add(this.pos.getZ());

        obj.add("pos", arr);
        obj.add("name", new JsonPrimitive(this.getName()));
        obj.add("rotation", new JsonPrimitive(this.rotation.name()));
        obj.add("mirror", new JsonPrimitive(this.mirror.name()));
        obj.add("locked_coords", new JsonPrimitive(0));
        obj.add("enabled", new JsonPrimitive(this.enabled));
        obj.add("rendering_enabled", new JsonPrimitive(true));
        obj.add("ignore_entities", new JsonPrimitive(this.ignoreEntities));

        return obj;
    }


    public enum RequiredEnabled {
        ANY,
        PLACEMENT_ENABLED,
        RENDERING_ENABLED
    }
}
