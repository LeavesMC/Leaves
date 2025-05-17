package org.leavesmc.leaves.protocol;

import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

@LeavesProtocol(namespace = "litematica-server-paster")
public class LMSPasterProtocol {

    public static final String MOD_ID = "litematica-server-paster";
    public static final String MOD_VERSION = "1.3.5";

    private static final ResourceLocation PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "network_v2");

    private static final Map<ServerGamePacketListenerImpl, StringBuilder> VERY_LONG_CHATS = new WeakHashMap<>();

    @ProtocolHandler.PayloadReceiver(payload = LmsPasterPayload.class, payloadId = "network_v2")
    public static void handlePackets(ServerPlayer player, LmsPasterPayload payload) {
        if (!LeavesConfig.protocol.lmsPasterProtocol) {
            return;
        }

        String playerName = player.getName().getString();
        int id = payload.getPacketId();
        CompoundTag nbt = payload.getNbt();
        switch (id) {
            case LMSPasterProtocol.C2S.HI -> {
                String clientModVersion = nbt.getString("mod_version").orElseThrow();
                LeavesLogger.LOGGER.info(String.format("Player %s connected with %s @ %s", playerName, LMSPasterProtocol.MOD_ID, clientModVersion));
                ProtocolUtils.sendPayloadPacket(player, LMSPasterProtocol.S2C.build(LMSPasterProtocol.S2C.HI, nbt2 -> nbt2.putString("mod_version", LMSPasterProtocol.MOD_VERSION)));
                ProtocolUtils.sendPayloadPacket(player, LMSPasterProtocol.S2C.build(LMSPasterProtocol.S2C.ACCEPT_PACKETS, nbt2 -> nbt2.putIntArray("ids", C2S.ALL_PACKET_IDS)));
            }
            case LMSPasterProtocol.C2S.CHAT -> {
                String message = nbt.getString("chat").orElseThrow();
                triggerCommand(player, playerName, message);
            }
            case LMSPasterProtocol.C2S.VERY_LONG_CHAT_START -> VERY_LONG_CHATS.put(player.connection, new StringBuilder());
            case LMSPasterProtocol.C2S.VERY_LONG_CHAT_CONTENT -> {
                String segment = nbt.getString("segment").orElseThrow();
                getVeryLongChatBuilder(player).ifPresent(builder -> builder.append(segment));
            }
            case LMSPasterProtocol.C2S.VERY_LONG_CHAT_END -> {
                getVeryLongChatBuilder(player).ifPresent(builder -> triggerCommand(player, playerName, builder.toString()));
                VERY_LONG_CHATS.remove(player.connection);
            }
        }
    }

    private static Optional<StringBuilder> getVeryLongChatBuilder(ServerPlayer player) {
        return Optional.ofNullable(VERY_LONG_CHATS.get(player.connection));
    }

    private static void triggerCommand(ServerPlayer player, String playerName, String command) {
        if (command.isEmpty()) {
            LeavesLogger.LOGGER.warning(String.format("Player %s sent an empty command", playerName));
        } else {
            player.getBukkitEntity().performCommand(command);
        }
    }

    private static class C2S {
        public static final int HI = 0;
        public static final int CHAT = 1;
        public static final int VERY_LONG_CHAT_START = 2;
        public static final int VERY_LONG_CHAT_CONTENT = 3;
        public static final int VERY_LONG_CHAT_END = 4;

        public static final int[] ALL_PACKET_IDS;

        static {
            Set<Integer> allPacketIds = Sets.newLinkedHashSet();
            for (Field field : C2S.class.getFields()) {
                if (field.getType() == int.class && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                    try {
                        allPacketIds.add((int) field.get(null));
                    } catch (Exception e) {
                        LeavesLogger.LOGGER.severe("Failed to initialized Lms Paster: ", e);
                    }
                }
            }
            ALL_PACKET_IDS = new int[allPacketIds.size()];
            int i = 0;
            for (Integer id : allPacketIds) {
                ALL_PACKET_IDS[i++] = id;
            }
        }
    }

    private static class S2C {
        public static final int HI = 0;
        public static final int ACCEPT_PACKETS = 1;

        public static CustomPacketPayload build(int packetId, Consumer<CompoundTag> payloadBuilder) {
            CompoundTag nbt = new CompoundTag();
            payloadBuilder.accept(nbt);
            return new LmsPasterPayload(packetId, nbt);
        }
    }

    public static class LmsPasterPayload implements LeavesCustomPayload<LmsPasterPayload> {
        private final int id;
        private final CompoundTag nbt;

        public LmsPasterPayload(int id, CompoundTag nbt) {
            this.id = id;
            this.nbt = nbt;
        }

        @New
        public LmsPasterPayload(ResourceLocation location, FriendlyByteBuf buf) {
            this(buf.readVarInt(), buf.readNbt());
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeVarInt(this.id);
            buf.writeNbt(this.nbt);
        }

        @Override
        public ResourceLocation id() {
            return PACKET_ID;
        }

        public int getPacketId() {
            return this.id;
        }

        public CompoundTag getNbt() {
            return this.nbt;
        }
    }
}