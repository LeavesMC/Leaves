package org.leavesmc.leaves.protocol.servux;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

// Powered by Servux(https://github.com/sakura-ryoko/servux)

/**
 * Network packet splitter code from QuickCarpet by skyrising
 *
 * @author skyrising
 * <p>
 * Updated by Sakura to work with newer versions by changing the Reading Session keys,
 * and using the HANDLER interface to send packets via the Payload system
 * <p>
 * Move to Leaves by violetc
 */
public class PacketSplitter {
    public static final int MAX_TOTAL_PER_PACKET_S2C = 1048576;
    public static final int MAX_PAYLOAD_PER_PACKET_S2C = MAX_TOTAL_PER_PACKET_S2C - 5;
    public static final int MAX_TOTAL_PER_PACKET_C2S = 32767;
    public static final int MAX_PAYLOAD_PER_PACKET_C2S = MAX_TOTAL_PER_PACKET_C2S - 5;
    public static final int DEFAULT_MAX_RECEIVE_SIZE_C2S = 1048576;
    public static final int DEFAULT_MAX_RECEIVE_SIZE_S2C = 67108864;

    private static final Map<Long, ReadingSession> READING_SESSIONS = new HashMap<>();

    public static boolean send(IPacketSplitterHandler handler, FriendlyByteBuf packet, ServerPlayer player) {
        return send(handler, packet, player, MAX_PAYLOAD_PER_PACKET_S2C);
    }

    private static boolean send(IPacketSplitterHandler handler, FriendlyByteBuf packet, ServerPlayer player, int payloadLimit) {
        int len = packet.writerIndex();

        packet.resetReaderIndex();

        for (int offset = 0; offset < len; offset += payloadLimit) {
            int thisLen = Math.min(len - offset, payloadLimit);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer(thisLen));

            buf.resetWriterIndex();

            if (offset == 0) {
                buf.writeVarInt(len);
            }

            buf.writeBytes(packet, thisLen);
            handler.encode(player, buf);
        }

        packet.release();

        return true;
    }

    public static FriendlyByteBuf receive(long key, FriendlyByteBuf buf) {
        return receive(key, buf, DEFAULT_MAX_RECEIVE_SIZE_S2C);
    }

    @Nullable
    private static FriendlyByteBuf receive(long key, FriendlyByteBuf buf, int maxLength) {
        return READING_SESSIONS.computeIfAbsent(key, ReadingSession::new).receive(buf, maxLength);
    }

    public interface IPacketSplitterHandler {
        void encode(ServerPlayer player, FriendlyByteBuf buf);
    }

    /**
     * I had to fix the `Pair.of` key mappings, because they were removed from MC;
     * So I made it into a pre-shared random session 'key' between client and server.
     * Generated using 'long key = Random.create(Util.getMeasuringTimeMs()).nextLong();'
     * -
     * It can be shared to the receiving end via a separate packet; or it can just be
     * generated randomly on the receiving end per an expected Reading Session.
     * It needs to be stored and changed for every unique session.
     */
    private static class ReadingSession {
        private final long key;
        private int expectedSize = -1;
        private FriendlyByteBuf received;

        private ReadingSession(long key) {
            this.key = key;
        }

        @Nullable
        private FriendlyByteBuf receive(FriendlyByteBuf data, int maxLength) {
            data.readerIndex(0);
            // data = PacketUtils.slice(data);

            if (this.expectedSize < 0) {
                this.expectedSize = data.readVarInt();

                if (this.expectedSize > maxLength) {
                    throw new IllegalArgumentException("Payload too large");
                }

                this.received = new FriendlyByteBuf(Unpooled.buffer(this.expectedSize));
            }

            this.received.writeBytes(data.readBytes(data.readableBytes()));

            if (this.received.writerIndex() >= this.expectedSize) {
                READING_SESSIONS.remove(this.key);
                return this.received;
            }

            return null;
        }
    }
}
