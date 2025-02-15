package org.leavesmc.leaves.protocol.servux;

import com.mojang.serialization.DataResult;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LeavesProtocol(namespace = "servux")
public class ServuxHudDataProtocol {

    public static final ResourceLocation CHANNEL = ServuxProtocol.id("hud_metadata");
    public static final int PROTOCOL_VERSION = 1;

    private static final List<ServerPlayer> players = new ArrayList<>();
    private static final int updateInterval = 80;

    public static boolean refreshSpawnMetadata = false;

    @ProtocolHandler.PayloadReceiver(payload = HudDataPayload.class, payloadId = "hud_metadata")
    public static void onPacketReceive(ServerPlayer player, HudDataPayload payload) {
        if (!LeavesConfig.protocol.servux.hudMetadataProtocol) {
            return;
        }

        switch (payload.packetType) {
            case PACKET_C2S_METADATA_REQUEST -> {
                players.add(player);

                CompoundTag metadata = new CompoundTag();
                metadata.putString("name", "hud_metadata");
                metadata.putString("id", CHANNEL.toString());
                metadata.putInt("version", PROTOCOL_VERSION);
                metadata.putString("servux", ServuxProtocol.SERVUX_STRING);
                putWorldData(metadata);

                sendPacket(player, new HudDataPayload(HudDataPayloadType.PACKET_S2C_METADATA, metadata));
            }

            case PACKET_C2S_SPAWN_DATA_REQUEST -> refreshSpawnMetadata(player);
            case PACKET_C2S_RECIPE_MANAGER_REQUEST -> refreshRecipeManager(player);
        }
    }

    @ProtocolHandler.Ticker
    public void onTick() {
        if (!LeavesConfig.protocol.servux.hudMetadataProtocol) {
            return;
        }

        MinecraftServer server = MinecraftServer.getServer();
        int tickCounter = server.getTickCount();
        if ((tickCounter % updateInterval) == 0) {
            for (ServerPlayer player : players) {
                if (refreshSpawnMetadata) {
                    refreshSpawnMetadata(player);
                }
                refreshWeatherData(player);
            }
            refreshSpawnMetadata = false;
        }
    }

    public static void refreshSpawnMetadata(ServerPlayer player) {
        if (!LeavesConfig.protocol.servux.hudMetadataProtocol) {
            return;
        }

        CompoundTag metadata = new CompoundTag();
        metadata.putString("id", CHANNEL.toString());
        metadata.putString("servux", ServuxProtocol.SERVUX_STRING);
        putWorldData(metadata);

        sendPacket(player, new HudDataPayload(HudDataPayloadType.PACKET_S2C_SPAWN_DATA, metadata));
    }

    public static void refreshRecipeManager(ServerPlayer player) {
        if (!LeavesConfig.protocol.servux.hudMetadataProtocol) {
            return;
        }

        Collection<RecipeHolder<?>> recipes = player.server.getRecipeManager().getRecipes();
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();

        recipes.forEach((recipeEntry -> {
            DataResult<Tag> dr = Recipe.CODEC.encodeStart(NbtOps.INSTANCE, recipeEntry.value());

            if (dr.result().isPresent()) {
                CompoundTag entry = new CompoundTag();
                entry.putString("id_reg", recipeEntry.id().registry().toString());
                entry.putString("id_value", recipeEntry.id().location().toString());
                entry.put("recipe", dr.result().get());
                list.add(entry);
            }
        }));

        nbt.put("RecipeManager", list);
        sendPacket(player, new HudDataPayload(HudDataPayloadType.PACKET_S2C_NBT_RESPONSE_START, nbt));
    }

    public static void refreshWeatherData(ServerPlayer player) {
        if (!LeavesConfig.protocol.servux.hudMetadataProtocol) {
            return;
        }

        ServerLevel level = MinecraftServer.getServer().overworld();
        if (level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
            return;
        }

        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", CHANNEL.toString());
        nbt.putString("servux", ServuxProtocol.SERVUX_STRING);

        if (level.serverLevelData.isRaining() && level.serverLevelData.getRainTime() > -1) {
            nbt.putInt("SetRaining", level.serverLevelData.getRainTime());
            nbt.putBoolean("isRaining", true);
        } else {
            nbt.putBoolean("isRaining", false);
        }

        if (level.serverLevelData.isThundering() && level.serverLevelData.getThunderTime() > -1) {
            nbt.putInt("SetThundering", level.serverLevelData.getThunderTime());
            nbt.putBoolean("isThundering", true);
        } else {
            nbt.putBoolean("isThundering", false);
        }

        if (level.serverLevelData.getClearWeatherTime() > -1) {
            nbt.putInt("SetClear", level.serverLevelData.getClearWeatherTime());
        }

        sendPacket(player, new HudDataPayload(HudDataPayloadType.PACKET_S2C_WEATHER_TICK, nbt));
    }

    private static void putWorldData(@NotNull CompoundTag metadata) {
        ServerLevel level = MinecraftServer.getServer().overworld();
        BlockPos spawnPos = level.levelData.getSpawnPos();
        metadata.putInt("spawnPosX", spawnPos.getX());
        metadata.putInt("spawnPosY", spawnPos.getY());
        metadata.putInt("spawnPosZ", spawnPos.getZ());
        metadata.putInt("spawnChunkRadius", level.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS));

        if (LeavesConfig.protocol.servux.hudMetadataShareSeed) {
            metadata.putLong("worldSeed", level.getSeed());
        }
    }

    public static void sendPacket(ServerPlayer player, HudDataPayload payload) {
        if (!LeavesConfig.protocol.servux.hudMetadataProtocol) {
            return;
        }

        if (payload.packetType == HudDataPayloadType.PACKET_S2C_NBT_RESPONSE_START) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeNbt(payload.nbt);
            PacketSplitter.send(ServuxHudDataProtocol::sendWithSplitter, buffer, player);
        } else {
            ProtocolUtils.sendPayloadPacket(player, payload);
        }
    }

    private static void sendWithSplitter(ServerPlayer player, FriendlyByteBuf buf) {
        sendPacket(player, new HudDataPayload(HudDataPayloadType.PACKET_S2C_NBT_RESPONSE_DATA, buf));
    }

    public enum HudDataPayloadType {
        PACKET_S2C_METADATA(1),
        PACKET_C2S_METADATA_REQUEST(2),
        PACKET_S2C_SPAWN_DATA(3),
        PACKET_C2S_SPAWN_DATA_REQUEST(4),
        PACKET_S2C_WEATHER_TICK(5),
        PACKET_C2S_RECIPE_MANAGER_REQUEST(6),
        // For Packet Splitter (Oversize Packets, S2C)
        PACKET_S2C_NBT_RESPONSE_START(10),
        PACKET_S2C_NBT_RESPONSE_DATA(11);

        private static final class Helper {
            static Map<Integer, HudDataPayloadType> ID_TO_TYPE = new HashMap<>();
        }

        public final int type;

        HudDataPayloadType(int type) {
            this.type = type;
            HudDataPayloadType.Helper.ID_TO_TYPE.put(type, this);
        }

        public static HudDataPayloadType fromId(int id) {
            return HudDataPayloadType.Helper.ID_TO_TYPE.get(id);
        }
    }

    public record HudDataPayload(HudDataPayloadType packetType, CompoundTag nbt, FriendlyByteBuf buffer) implements LeavesCustomPayload<HudDataPayload> {

        public HudDataPayload(HudDataPayloadType type, CompoundTag nbt) {
            this(type, nbt, null);
        }

        public HudDataPayload(HudDataPayloadType type, FriendlyByteBuf buffer) {
            this(type, new CompoundTag(), buffer);
        }

        @New
        @NotNull
        public static HudDataPayload decode(ResourceLocation location, @NotNull FriendlyByteBuf buf) {
            HudDataPayloadType type = HudDataPayloadType.fromId(buf.readVarInt());
            if (type == null) {
                throw new IllegalStateException("invalid packet type received");
            }

            switch (type) {
                case PACKET_S2C_NBT_RESPONSE_DATA -> {
                    return new HudDataPayload(type, new FriendlyByteBuf(buf.readBytes(buf.readableBytes())));
                }

                case PACKET_C2S_METADATA_REQUEST, PACKET_S2C_METADATA, PACKET_C2S_SPAWN_DATA_REQUEST, PACKET_S2C_SPAWN_DATA, PACKET_S2C_WEATHER_TICK,
                     PACKET_C2S_RECIPE_MANAGER_REQUEST -> {
                    return new HudDataPayload(type, buf.readNbt());
                }
            }

            throw new IllegalStateException("invalid packet type received");
        }

        @Override
        public void write(@NotNull FriendlyByteBuf buf) {
            buf.writeVarInt(this.packetType.type);

            switch (this.packetType) {
                case PACKET_S2C_NBT_RESPONSE_DATA -> buf.writeBytes(this.buffer.copy());
                case PACKET_C2S_METADATA_REQUEST, PACKET_S2C_METADATA, PACKET_C2S_SPAWN_DATA_REQUEST,
                     PACKET_S2C_SPAWN_DATA, PACKET_S2C_WEATHER_TICK, PACKET_C2S_RECIPE_MANAGER_REQUEST -> buf.writeNbt(this.nbt);
            }
        }

        @Override
        public ResourceLocation id() {
            return CHANNEL;
        }
    }
}
