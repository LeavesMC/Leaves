package org.leavesmc.leaves.protocol.chatimage;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Contract;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.leavesmc.leaves.protocol.chatimage.ServerBlockCache.SERVER_BLOCK_CACHE;

@LeavesProtocol(namespace = "chatimage")
public class ChatImageProtocol {

    public static final String PROTOCOL_ID = "chatimage";
    public static final Gson gson = new Gson();

    @Contract("_ -> new")
    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(PROTOCOL_ID, path);
    }

    @ProtocolHandler.PayloadReceiver(payload = FileChannelPayload.class, key = "get_file_channel")
    public static void serverFileChannelReceived(ServerPlayer player, FileChannelPayload payload) {
        if (!LeavesConfig.protocol.chatImageProtocol) {
            return;
        }

        MinecraftServer server = MinecraftServer.getServer();
        String res = payload.message();
        ChatImageIndex title = gson.fromJson(res, ChatImageIndex.class);
        Map<Integer, String> blocks = SERVER_BLOCK_CACHE.createBlock(title, res);
        if (title.total != blocks.size()) {
            return;
        }
        List<UUID> names = SERVER_BLOCK_CACHE.getUsers(title.url);
        if (names == null || player == null) {
            return;
        }
        for (UUID uuid : names) {
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(uuid);
            if (serverPlayer != null) {
                ProtocolUtils.sendPayloadPacket(serverPlayer, new FileInfoChannelPayload("true->" + title.url));
            }
        }
    }

    @ProtocolHandler.PayloadReceiver(payload = FileInfoChannelPayload.class, key = "file_info")
    public static void serverGetFileChannelReceived(ServerPlayer player, FileInfoChannelPayload packet) {
        if (!LeavesConfig.protocol.chatImageProtocol) {
            return;
        }

        String url = packet.message();
        Map<Integer, String> list = SERVER_BLOCK_CACHE.getBlock(url);
        if (list == null) {
            ProtocolUtils.sendPayloadPacket(player, new FileInfoChannelPayload("null->" + url));
            SERVER_BLOCK_CACHE.tryAddUser(url, player.getUUID());
            return;
        }
        for (Map.Entry<Integer, String> entry : list.entrySet()) {
            ProtocolUtils.sendPayloadPacket(player, new DownloadFileChannelPayload(entry.getValue()));
        }
    }
}