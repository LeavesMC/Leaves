package org.leavesmc.leaves.network;

import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class AsyncKeepaliveManager {

    private static final LeavesLogger LOGGER = LeavesLogger.LOGGER;
    private static final Map<Connection, ServerCommonPacketListenerImpl> ACTIVE_LISTENERS = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Leaves Async Keepalive");
        thread.setDaemon(true);
        return thread;
    });

    static {
        if (LeavesConfig.mics.asyncKeepalive.enable) {
            EXECUTOR.scheduleAtFixedRate(AsyncKeepaliveManager::tickAll, 1L, 1L, TimeUnit.SECONDS);
        }
    }

    private AsyncKeepaliveManager() {
    }

    public static void register(ServerCommonPacketListenerImpl listener) {
        if (!LeavesConfig.mics.asyncKeepalive.enable) {
            return;
        }

        ACTIVE_LISTENERS.put(listener.connection, listener);
    }

    public static void unregister(ServerCommonPacketListenerImpl listener) {
        ACTIVE_LISTENERS.remove(listener.connection, listener);
    }

    private static void tickAll() {
        long currentTimeNs = System.nanoTime();
        long currentTimeMs = Util.getMillis();

        for (ServerCommonPacketListenerImpl listener : ACTIVE_LISTENERS.values()) {
            try {
                listener.leaves$keepConnectionAliveAsync(currentTimeNs, currentTimeMs);
                if (!listener.connection.isConnected() || listener.processedDisconnect) {
                    ACTIVE_LISTENERS.remove(listener.connection, listener);
                }
            } catch (Throwable throwable) {
                ACTIVE_LISTENERS.remove(listener.connection, listener);
                LOGGER.log(Level.SEVERE, "Failed to run async keepalive for connection " + listener.connection.getRemoteAddress(), throwable);
            }
        }
    }
}
