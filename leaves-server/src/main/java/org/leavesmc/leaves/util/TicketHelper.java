package org.leavesmc.leaves.util;

import ca.spottedleaf.moonrise.patches.chunk_system.player.RegionizedPlayerChunkLoader;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelResource;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;

public class TicketHelper {

    private static final Set<TicketType<?>> NEED_SAVED = Set.of(TicketType.PLAYER, TicketType.PORTAL, RegionizedPlayerChunkLoader.PLAYER_TICKET);

    public static void tryToLoadTickets() {
        if (!LeavesConfig.modify.fastResume) {
            return;
        }

        File file = MinecraftServer.getServer().getWorldPath(LevelResource.ROOT).resolve("chunk_tickets.leaves.json").toFile();
        if (file.isFile()) {
            try (BufferedReader bfr = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                JsonObject json = new Gson().fromJson(bfr, JsonObject.class);
                loadSavedChunkTickets(json);
                if (!file.delete()) {
                    throw new IOException();
                }
            } catch (IOException e) {
                LeavesLogger.LOGGER.severe("Failed to load saved chunk tickets file", e);
            }
        }
    }

    public static void tryToSaveTickets() {
        if (!LeavesConfig.modify.fastResume) {
            return;
        }

        File file = MinecraftServer.getServer().getWorldPath(LevelResource.ROOT).resolve("chunk_tickets.leaves.json").toFile();
        try (BufferedWriter bfw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            bfw.write(new Gson().toJson(getSavedChunkTickets()));
        } catch (IOException e) {
            LeavesLogger.LOGGER.severe("Failed to save chunk tickets file", e);
        }
    }

    public static void loadSavedChunkTickets(JsonObject json) {
        MinecraftServer server = MinecraftServer.getServer();
        for (String worldKey : json.keySet()) {
            ResourceLocation dimensionKey = ResourceLocation.tryParse(worldKey);
            if (dimensionKey == null) {
                continue;
            }

            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionKey));
            if (level == null) {
                continue;
            }

            DistanceManager chunkDistanceManager = level.getChunkSource().chunkMap.distanceManager;
            for (JsonElement chunkElement : json.get(worldKey).getAsJsonArray()) {
                JsonObject chunkJson = (JsonObject) chunkElement;
                long chunkKey = chunkJson.get("key").getAsLong();

                for (JsonElement ticketElement : chunkJson.get("tickets").getAsJsonArray()) {
                    Ticket<?> ticket = tickFormJson((JsonObject) ticketElement);
                    chunkDistanceManager.moonrise$getChunkHolderManager().addTicketAtLevelCustom(ticket, chunkKey, true);
                }
            }
        }
    }

    public static JsonObject getSavedChunkTickets() {
        JsonObject json = new JsonObject();

        for (ServerLevel level : MinecraftServer.getServer().getAllLevels()) {
            JsonArray levelArray = new JsonArray();
            DistanceManager chunkDistanceManager = level.getChunkSource().chunkMap.distanceManager;

            for (Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> chunkTickets : chunkDistanceManager.moonrise$getChunkHolderManager().getTicketsCopy().long2ObjectEntrySet()) {
                long chunkKey = chunkTickets.getLongKey();
                JsonArray ticketArray = new JsonArray();
                SortedArraySet<Ticket<?>> tickets = chunkTickets.getValue();

                for (Ticket<?> ticket : tickets) {
                    if (!NEED_SAVED.contains(ticket.getType())) {
                        continue;
                    }

                    ticketArray.add(ticketToJson(ticket));
                }

                if (!ticketArray.isEmpty()) {
                    JsonObject chunkJson = new JsonObject();
                    chunkJson.addProperty("key", chunkKey);
                    chunkJson.add("tickets", ticketArray);
                    levelArray.add(chunkJson);
                }
            }

            if (!levelArray.isEmpty()) {
                json.add(level.dimension().location().toString(), levelArray);
            }
        }

        return json;
    }

    private static JsonObject ticketToJson(Ticket<?> ticket) {
        JsonObject json = new JsonObject();
        json.addProperty("type", ticket.getType().toString());
        json.addProperty("ticketLevel", ticket.getTicketLevel());
        json.addProperty("removeDelay", ticket.moonrise$getRemoveDelay());
        if (ticket.key instanceof BlockPos pos) {
            json.addProperty("key", pos.asLong());
        } else if (ticket.key instanceof ChunkPos pos) {
            json.addProperty("key", pos.toLong());
        } else if (ticket.key instanceof Long l) {
            json.addProperty("key", l);
        }
        return json;
    }

    private static <T> Ticket<T> tickFormJson(JsonObject json) {
        TicketType<?> ticketType = null;
        Object key = null;
        switch (json.get("type").getAsString()) {
            case "player" -> {
                ticketType = TicketType.PLAYER;
                key = new ChunkPos(json.get("key").getAsLong());
            }
            case "portal" -> {
                ticketType = TicketType.PORTAL;
                key = BlockPos.of(json.get("key").getAsLong());
            }
            case "chunk_system:player_ticket" -> {
                ticketType = RegionizedPlayerChunkLoader.PLAYER_TICKET;
                key = json.get("key").getAsLong();
            }
        }

        if (ticketType == null) {
            throw new IllegalArgumentException("Cant convert " + json.get("type").getAsString() + ", report it ???");
        }

        int ticketLevel = json.get("ticketLevel").getAsInt();
        long removeDelay = json.get("removeDelay").getAsLong();
        @SuppressWarnings("unchecked")
        Ticket<T> ticket = new Ticket<>((TicketType<T>) ticketType, ticketLevel, (T) key);
        ticket.moonrise$setRemoveDelay(removeDelay);

        return ticket;
    }
}
