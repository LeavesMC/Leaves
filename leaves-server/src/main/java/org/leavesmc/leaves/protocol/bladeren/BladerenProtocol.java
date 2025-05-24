package org.leavesmc.leaves.protocol.bladeren;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
import java.util.Map;
import java.util.function.BiConsumer;

@LeavesProtocol.Register(namespace = "bladeren")
public class BladerenProtocol implements LeavesProtocol {

    public static final String PROTOCOL_ID = "bladeren";
    public static final String PROTOCOL_VERSION = ProtocolUtils.buildProtocolVersion(PROTOCOL_ID);

    private static final Map<String, BiConsumer<ServerPlayer, CompoundTag>> registeredFeatures = new HashMap<>();

    @Contract("_ -> new")
    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(PROTOCOL_ID, path);
    }

    @ProtocolHandler.PayloadReceiver(payload = BladerenHelloPayload.class)
    private static void handleHello(@NotNull ServerPlayer player, @NotNull BladerenHelloPayload payload) {
        String clientVersion = payload.version;
        CompoundTag tag = payload.nbt;

        LeavesLogger.LOGGER.info("Player " + player.getScoreboardName() + " joined with bladeren " + clientVersion);

        if (tag != null) {
            CompoundTag featureNbt = tag.getCompound("Features").orElseThrow();
            for (String name : featureNbt.keySet()) {
                if (registeredFeatures.containsKey(name)) {
                    registeredFeatures.get(name).accept(player, featureNbt.getCompound(name).orElseThrow());
                }
            }
        }
    }

    @ProtocolHandler.PayloadReceiver(payload = BladerenFeatureModifyPayload.class)
    private static void handleModify(@NotNull ServerPlayer player, @NotNull BladerenFeatureModifyPayload payload) {
        String name = payload.name;
        CompoundTag tag = payload.nbt;

        if (registeredFeatures.containsKey(name)) {
            registeredFeatures.get(name).accept(player, tag);
        }
    }

    @ProtocolHandler.PlayerJoin
    public static void onPlayerJoin(@NotNull ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        LeavesFeatureSet.writeNBT(tag);
        ProtocolUtils.sendPayloadPacket(player, new BladerenHelloPayload(PROTOCOL_VERSION, tag));
    }

    public static void registerFeature(String name, BiConsumer<ServerPlayer, CompoundTag> consumer) {
        registeredFeatures.put(name, consumer);
    }

    @Override
    public boolean isActive() {
        return LeavesConfig.protocol.bladeren.enable;
    }

    public static class LeavesFeatureSet {

        private static final Map<String, LeavesFeature> features = new HashMap<>();

        public static void writeNBT(@NotNull CompoundTag tag) {
            CompoundTag featureNbt = new CompoundTag();
            features.values().forEach(feature -> feature.writeNBT(featureNbt));
            tag.put("Features", featureNbt);
        }

        public static void register(LeavesFeature feature) {
            features.put(feature.name, feature);
        }
    }

    public record LeavesFeature(String name, String value) {

        @NotNull
        @Contract("_, _ -> new")
        public static LeavesFeature of(String name, boolean value) {
            return new LeavesFeature(name, Boolean.toString(value));
        }

        public void writeNBT(@NotNull CompoundTag rules) {
            CompoundTag rule = new CompoundTag();
            rule.putString("Feature", name);
            rule.putString("Value", value);
            rules.put(name, rule);
        }
    }

    public record BladerenFeatureModifyPayload(String name, CompoundTag nbt) implements LeavesCustomPayload {

        @ID
        private static final ResourceLocation FEATURE_MODIFY_ID = id("feature_modify");

        @Codec
        private static final StreamCodec<FriendlyByteBuf, BladerenFeatureModifyPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.name());
                buf.writeNbt(payload.nbt());
            },
            buffer -> new BladerenFeatureModifyPayload(buffer.readUtf(), buffer.readNbt())
        );
    }

    public record BladerenHelloPayload(String version, CompoundTag nbt) implements LeavesCustomPayload {

        @ID
        private static final ResourceLocation HELLO_ID = id("hello");

        @Codec
        private static final StreamCodec<FriendlyByteBuf, BladerenHelloPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.version());
                buf.writeNbt(payload.nbt());
            },
            buffer -> new BladerenHelloPayload(buffer.readUtf(64), buffer.readNbt())
        );
    }
}