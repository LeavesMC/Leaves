package org.leavesmc.leaves.protocol.servux.litematics.schematic.utils;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.Profiler;

import java.util.List;

public interface IDataProvider
{
    /**
     * Returns the simple name for this data provider.
     * This should preferably be a lower case alphanumeric string with no
     * other special characters than '-' and '_'.
     * This name will be used in the enable/disable commands as the argument
     * and also as the config file key/identifier.
     * @return
     */
    String getName();

    /**
     * Returns the description of this data provider.
     * Used in the command to list the available providers and to check the status
     * of a given provider.
     * @return
     */
    String getDescription();

    /**
     * Returns the network channel name used by this data provider to listen
     * for incoming data requests and to respond and send the requested data.
     * @return
     */
    ResourceLocation getNetworkChannel();

    /**
     * Returns the current protocol version this provider supports
     * @return
     */
    int getProtocolVersion();

    /**
     * Returns true if this data provider is currently enabled.
     * @return
     */
    boolean isEnabled();

    /**
     * Enables or disables this data provider
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * Returns true if this data provider is currently Play Registered,
     * This is meant to stop Servux from trying to re-register the Play Channel
     * @return
     */
    boolean isRegistered();

    /**
     * Marks this Data Provider as Play Registered.
     * This is meant to stop Servux from trying to re-register the Play Channel
     * @param toggle
     */
    void setRegistered(boolean toggle);

    /**
     * Informs this data provider that the server has started and should be waiting for requests
     * @return
     */
    void registerHandler();

    /**
     * Informs this data provider that the server has stopped and should no longer process any data
     * @return
     */
    void unregisterHandler();

    /**
     * Returns whether or not this data provider should get ticked to periodically send some data,
     * or if it's only listening for incoming requests and responds to them directly.
     * @return
     */
    default boolean shouldTick()
    {
        return false;
    }

    /**
     * Returns the interval in game ticks that this data provider should be ticked at
     * @return
     */
    int getTickInterval();

    /**
     * Called at the given tick rate
     * @param server
     * @param tickCounter The current server tick (since last server start)
     */
    default void tick(MinecraftServer server, int tickCounter, Profiler profiler)
    {
    }

    /**
     * Returns the network packet handler used for this data provider.
     * @return ()
     */
    // default IPluginServerPlayHandler<?> getPacketHandler() { return null; }

    /**
     * Return if this player is registered to use this Data Provider
     * @param player (Player to be checked)
     * @return (True|False)
     */
    boolean isPlayerRegistered(ServerPlayer player);

    /**
     * Determine if Player has permissions to this Data Provider
     * @param player (Player to test permissions for)
     * @return (true|false)
     */
    boolean hasPermission(ServerPlayer player);

    /**
     * Signal The Data Providers when the server is shutting down
     * (Pre / Post)
     */
    void onTickEndPre();
    void onTickEndPost();

    /**
     * Config file handling
     * @return
     */
    JsonObject toJson();

    void fromJson(JsonObject obj);

    // List<IServuxSetting<?>> getSettings();
}
