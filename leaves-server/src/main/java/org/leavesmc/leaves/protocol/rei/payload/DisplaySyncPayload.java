package org.leavesmc.leaves.protocol.rei.payload;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.protocol.rei.REIServerProtocol;
import org.leavesmc.leaves.protocol.rei.display.Display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

public record DisplaySyncPayload(
    SyncType syncType,
    Collection<Display> displays,
    long version
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DisplaySyncPayload> TYPE = new CustomPacketPayload.Type<>(REIServerProtocol.SYNC_DISPLAYS_PACKET);
    public static final StreamCodec<? super RegistryFriendlyByteBuf, DisplaySyncPayload> STREAM_CODEC = StreamCodec.composite(
        SyncType.STREAM_CODEC,
        DisplaySyncPayload::syncType,
        Display.dispatchCodec().apply(codec -> new StreamCodec<RegistryFriendlyByteBuf, Display>() {
                @Override
                public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull Display display) {
                    RegistryFriendlyByteBuf tmpBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(), buf.registryAccess());
                    try {
                        codec.encode(tmpBuf, display);
                    } catch (Exception e) {
                        tmpBuf.release();
                        buf.writeBoolean(false);
                        LeavesLogger.LOGGER.warning("Failed to encode display: " + display, e);
                        return;
                    }
                    buf.writeBoolean(true);
                    RegistryFriendlyByteBuf.writeByteArray(buf, ByteBufUtil.getBytes(tmpBuf));
                    tmpBuf.release();
                }

                @NotNull
                @Override
                public Display decode(@NotNull RegistryFriendlyByteBuf buf) {
                    // The DisplayDecoder will not be called on the server side
                    throw new UnsupportedOperationException();
                }
            }
        ).apply(ByteBufCodecs.<RegistryFriendlyByteBuf, Display, Collection<Display>>collection(ArrayList::new)).map(
            collection -> collection.stream().filter(Objects::nonNull).toList(),
            UnaryOperator.identity()
        ),
        DisplaySyncPayload::displays,
        ByteBufCodecs.LONG,
        DisplaySyncPayload::version,
        DisplaySyncPayload::new
    );

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum SyncType {
        APPEND,
        SET;

        public static final IntFunction<SyncType> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, SyncType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
    }
}
