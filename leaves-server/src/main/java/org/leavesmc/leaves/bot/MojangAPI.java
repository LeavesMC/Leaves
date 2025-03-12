package org.leavesmc.leaves.bot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.leavesmc.leaves.LeavesConfig;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MojangAPI {

    private static final Map<String, String[]> CACHE = new HashMap<>();

    public static String[] getSkin(String name) {
        if (LeavesConfig.modify.fakeplayer.useSkinCache && CACHE.containsKey(name)) {
            return CACHE.get(name);
        }

        String[] values = pullFromAPI(name);
        CACHE.put(name, values);
        return values;
    }

    // Laggggggggggggggggggggggggggggggggggggggggg
    public static String[] pullFromAPI(String name) {
        try {
            String uuid = JsonParser.parseReader(new InputStreamReader(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name).toURL().openStream()))
                .getAsJsonObject().get("id").getAsString();
            JsonObject property = JsonParser.parseReader(new InputStreamReader(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false").toURL().openStream()))
                .getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            return new String[]{property.get("value").getAsString(), property.get("signature").getAsString()};
        } catch (IOException | IllegalStateException | IllegalArgumentException e) {
            return null;
        }
    }
}
