package org.leavesmc.leaves.protocol;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@LeavesProtocol.Register(namespace = "carpet")
public class CarpetServerProtocol implements LeavesProtocol {

    public static final String PROTOCOL_ID = "carpet";
    public static final String VERSION = ProtocolUtils.buildProtocolVersion(PROTOCOL_ID);

    private static final String HI = "69";
    private static final String HELLO = "420";

    @Contract("_ -> new")
    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(PROTOCOL_ID, path);
    }

    @ProtocolHandler.PlayerJoin
    public static void onPlayerJoin(ServerPlayer player) {
        if (LeavesConfig.protocol.leavesCarpetSupport) {
            CompoundTag data = new CompoundTag();
            data.putString(HI, VERSION);
            ProtocolUtils.sendPayloadPacket(player, new CarpetPayload(data));
        }
    }

    @ProtocolHandler.PayloadReceiver(payload = CarpetPayload.class)
    private static void handleHello(@NotNull ServerPlayer player, @NotNull CarpetServerProtocol.CarpetPayload payload) {
        if (LeavesConfig.protocol.leavesCarpetSupport) {
            if (payload.nbt.contains(HELLO)) {
                LeavesLogger.LOGGER.info("Player " + player.getScoreboardName() + " joined with carpet " + payload.nbt.getString(HELLO));
                CompoundTag data = new CompoundTag();
                CarpetRules.write(data);
                ProtocolUtils.sendPayloadPacket(player, new CarpetPayload(data));
            }
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }

    public static class CarpetRules {

        private static final Map<String, CarpetRule> rules = new HashMap<>();

        public static void write(@NotNull CompoundTag tag) {
            CompoundTag rulesNbt = new CompoundTag();
            rules.values().forEach(rule -> rule.writeNBT(rulesNbt));

            tag.put("Rules", rulesNbt);
        }

        public static void register(CarpetRule rule) {
            rules.put(rule.name, rule);
        }
    }

    public record CarpetRule(String identifier, String name, String value) {

        @NotNull
        @Contract("_, _, _ -> new")
        public static CarpetRule of(String identifier, String name, Enum<?> value) {
            return new CarpetRule(identifier, name, value.name().toLowerCase(Locale.ROOT));
        }

        @NotNull
        @Contract("_, _, _ -> new")
        public static CarpetRule of(String identifier, String name, boolean value) {
            return new CarpetRule(identifier, name, Boolean.toString(value));
        }

        public void writeNBT(@NotNull CompoundTag rules) {
            CompoundTag rule = new CompoundTag();
            String key = name;

            while (rules.contains(key)) {
                key = key + "2";
            }

            rule.putString("Value", value);
            rule.putString("Manager", identifier);
            rule.putString("Rule", name);
            rules.put(key, rule);
        }
    }

    public record CarpetPayload(CompoundTag nbt) implements LeavesCustomPayload {
        @ID
        private static final ResourceLocation HELLO_ID = CarpetServerProtocol.id("hello");

        @Codec
        private static final StreamCodec<FriendlyByteBuf, CarpetPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, CarpetPayload::nbt, CarpetPayload::new
        );
    }
}
