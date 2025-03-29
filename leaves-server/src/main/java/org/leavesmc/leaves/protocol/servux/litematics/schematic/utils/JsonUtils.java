package org.leavesmc.leaves.protocol.servux.litematics.schematic.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class JsonUtils {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean hasString(JsonObject obj, String name) {
        JsonElement el = obj.get(name);

        if (el != null && el.isJsonPrimitive()) {
            try {
                el.getAsString();
                return true;
            } catch (Exception ignore) {
            }
        }

        return false;
    }

    public static boolean hasArray(JsonObject obj, String name) {
        JsonElement el = obj.get(name);
        return el != null && el.isJsonArray();
    }

    public static boolean getBooleanOrDefault(JsonObject obj, String name, boolean defaultValue) {
        if (obj.has(name) && obj.get(name).isJsonPrimitive()) {
            try {
                return obj.get(name).getAsBoolean();
            } catch (Exception ignore) {
            }
        }

        return defaultValue;
    }

    public static int getIntegerOrDefault(JsonObject obj, String name, int defaultValue) {
        if (obj.has(name) && obj.get(name).isJsonPrimitive()) {
            try {
                return obj.get(name).getAsInt();
            } catch (Exception ignore) {
            }
        }

        return defaultValue;
    }

    public static long getLongOrDefault(JsonObject obj, String name, long defaultValue) {
        if (obj.has(name) && obj.get(name).isJsonPrimitive()) {
            try {
                return obj.get(name).getAsLong();
            } catch (Exception ignore) {
            }
        }

        return defaultValue;
    }

    public static float getFloatOrDefault(JsonObject obj, String name, float defaultValue) {
        if (obj.has(name) && obj.get(name).isJsonPrimitive()) {
            try {
                return obj.get(name).getAsFloat();
            } catch (Exception ignore) {
            }
        }

        return defaultValue;
    }

    public static double getDoubleOrDefault(JsonObject obj, String name, double defaultValue) {
        if (obj.has(name) && obj.get(name).isJsonPrimitive()) {
            try {
                return obj.get(name).getAsDouble();
            } catch (Exception ignore) {
            }
        }

        return defaultValue;
    }

    public static String getStringOrDefault(JsonObject obj, String name, String defaultValue) {
        if (obj.has(name) && obj.get(name).isJsonPrimitive()) {
            try {
                return obj.get(name).getAsString();
            } catch (Exception ignore) {
            }
        }

        return defaultValue;
    }

    public static boolean getBoolean(JsonObject obj, String name) {
        return getBooleanOrDefault(obj, name, false);
    }

    public static int getInteger(JsonObject obj, String name) {
        return getIntegerOrDefault(obj, name, 0);
    }

    public static long getLong(JsonObject obj, String name) {
        return getLongOrDefault(obj, name, 0);
    }

    public static float getFloat(JsonObject obj, String name) {
        return getFloatOrDefault(obj, name, 0);
    }

    public static double getDouble(JsonObject obj, String name) {
        return getDoubleOrDefault(obj, name, 0);
    }

    @Nullable
    public static String getString(JsonObject obj, String name) {
        return getStringOrDefault(obj, name, null);
    }

    public static JsonArray blockPosToJson(Vec3i pos) {
        JsonArray arr = new JsonArray();

        arr.add(pos.getX());
        arr.add(pos.getY());
        arr.add(pos.getZ());

        return arr;
    }

    @Nullable
    public static BlockPos blockPosFromJson(JsonObject obj, String name) {
        if (hasArray(obj, name)) {
            JsonArray arr = obj.getAsJsonArray(name);

            if (arr.size() == 3) {
                try {
                    return new BlockPos(arr.get(0).getAsInt(), arr.get(1).getAsInt(), arr.get(2).getAsInt());
                } catch (Exception ignore) {
                }
            }
        }

        return null;
    }

    // https://stackoverflow.com/questions/29786197/gson-jsonobject-copy-value-affected-others-jsonobject-instance
    @Nonnull
    public static JsonObject deepCopy(@Nonnull JsonObject jsonObject) {
        JsonObject result = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            result.add(entry.getKey(), deepCopy(entry.getValue()));
        }

        return result;
    }

    @Nonnull
    public static JsonArray deepCopy(@Nonnull JsonArray jsonArray) {
        JsonArray result = new JsonArray();

        for (JsonElement e : jsonArray) {
            result.add(deepCopy(e));
        }

        return result;
    }

    @Nonnull
    public static JsonElement deepCopy(@Nonnull JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive() || jsonElement.isJsonNull()) {
            return jsonElement; // these are immutable anyway
        } else if (jsonElement.isJsonObject()) {
            return deepCopy(jsonElement.getAsJsonObject());
        } else if (jsonElement.isJsonArray()) {
            return deepCopy(jsonElement.getAsJsonArray());
        } else {
            throw new UnsupportedOperationException("Unsupported element: " + jsonElement);
        }
    }
}
