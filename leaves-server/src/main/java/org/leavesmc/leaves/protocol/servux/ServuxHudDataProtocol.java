package org.leavesmc.leaves.protocol.servux;

import com.mojang.serialization.DataResult;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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

@LeavesProtocol.Register(namespace = "servux")
public class ServuxHudDataProtocol implements LeavesProtocol {

    public static final int PROTOCOL_VERSION = 1;

    private static final List<ServerPlayer> players = new ArrayList<>();
    private static final int updateInterval = 80;

    public static boolean refreshSpawnMetadata = false;

    @ProtocolHandler.PayloadReceiver(payload = HudDataPayload.class)
    public static void onPacketReceive(ServerPlayer player, HudDataPayload payload) {
        switch (payload.packetType) {
            case PACKET_C2S_METADATA_REQUEST -> {
                players.add(player);

                CompoundTag metadata = new CompoundTag();
                metadata.putString("name", "hud_metadata");
                metadata.putString("id", HudDataPayload.CHANNEL.toString());
                metadata.putInt("version", PROTOCOL_VERSION);
                metadata.putString("servux", ServuxProtocol.SERVUX_STRING);
                putWorldData(metadata);

                sendPacket(player, new HudDataPayload(HudDataPayloadType.PACKET_S2C_METADATA, metadata));
            }

            case PACKET_C2S_SPAWN_DATA_REQUEST -> refreshSpawnMetadata(player);
            case PACKET_C2S_RECIPE_MANAGER_REQUEST -> refreshRecipeManager(player);
        }
    }

    public static void refreshSpawnMetadata(ServerPlayer player) {
        CompoundTag metadata = new CompoundTag();
        metadata.putString("id", HudDataPayload.CHANNEL.toString());
        metadata.putString("servux", ServuxProtocol.SERVUX_STRING);
        putWorldData(metadata);

        sendPacket(player, new HudDataPayload(HudDataPayloadType.PACKET_S2C_SPAWN_DATA, metadata));
    }

    public static void refreshRecipeManager(ServerPlayer player) {
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
        ServerLevel level = MinecraftServer.getServer().overworld();
        if (level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
            return;
        }

        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", HudDataPayload.CHANNEL.toString());
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

    @ProtocolHandler.Ticker
    public void onTick() {
        for (ServerPlayer player : players) {
            if (refreshSpawnMetadata) {
                refreshSpawnMetadata(player);
            }
            refreshWeatherData(player);
        }
        refreshSpawnMetadata = false;
    }

    @Override
    public boolean isActive() {
        return LeavesConfig.protocol.servux.hudMetadataProtocol;
    }

    @Override
    public int tickerInterval(String tickerID) {
        return updateInterval;
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

        public final int type;

        HudDataPayloadType(int type) {
            this.type = type;
            HudDataPayloadType.Helper.ID_TO_TYPE.put(type, this);
        }

        public static HudDataPayloadType fromId(int id) {
            return HudDataPayloadType.Helper.ID_TO_TYPE.get(id);
        }

        private static final class Helper {
            static Map<Integer, HudDataPayloadType> ID_TO_TYPE = new HashMap<>();
        }
    }

    public record HudDataPayload(HudDataPayloadType packetType, CompoundTag nbt, FriendlyByteBuf buffer) implements LeavesCustomPayload {

        @ID
        public static final ResourceLocation CHANNEL = ServuxProtocol.id("hud_metadata");

        @Codec
        public static final StreamCodec<FriendlyByteBuf, HudDataPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.packetType().type);
                switch (payload.packetType()) {
                    case PACKET_S2C_NBT_RESPONSE_DATA -> buf.writeBytes(payload.buffer().copy());
                    case PACKET_C2S_METADATA_REQUEST, PACKET_S2C_METADATA, PACKET_C2S_SPAWN_DATA_REQUEST,
                         PACKET_S2C_SPAWN_DATA, PACKET_S2C_WEATHER_TICK, PACKET_C2S_RECIPE_MANAGER_REQUEST -> buf.writeNbt(payload.nbt());
                }
            },
            buf -> {
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
        );

        public HudDataPayload(HudDataPayloadType type, CompoundTag nbt) {
            this(type, nbt, null);
        }

        public HudDataPayload(HudDataPayloadType type, FriendlyByteBuf buffer) {
            this(type, new CompoundTag(), buffer);
        }

    }
}