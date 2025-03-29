package org.leavesmc.leaves.protocol.servux.litematics.schematic.utils;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.profiling.Profiler;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class DataProviderManager {
    public static final DataProviderManager INSTANCE = new DataProviderManager();

    /**
     * lower case name to data provider instances.
     */
    protected final HashMap<String, IDataProvider> providers = new HashMap<>();
    protected ImmutableList<IDataProvider> providersImmutable = ImmutableList.of();
    protected ArrayList<IDataProvider> providersTicking = new ArrayList<>();

    public ImmutableList<IDataProvider> getAllProviders() {
        return this.providersImmutable;
    }

    protected Path configDir = null;
    protected RegistryAccess.Frozen immutable = RegistryAccess.EMPTY;

    /**
     * Registers the given data provider, if it's not already registered
     *
     * @param provider ()
     * @return true if the provider did not exist yet and was successfully registered
     */
    public boolean registerDataProvider(IDataProvider provider) {
        String name = provider.getName().toLowerCase();

        if (this.providers.containsKey(name) == false) {
            this.providers.put(name, provider);
            this.providersImmutable = ImmutableList.copyOf(this.providers.values());

            return true;
        }

        return false;
    }

    public boolean setProviderEnabled(String providerName, boolean enabled) {
        IDataProvider provider = this.providers.get(providerName);
        return provider != null && this.setProviderEnabled(provider, enabled);
    }

    public boolean setProviderEnabled(IDataProvider provider, boolean enabled) {
        boolean wasEnabled = provider.isEnabled();

        if (enabled || wasEnabled != enabled) {
            provider.setEnabled(enabled);
            this.updatePacketHandlerRegistration(provider);

            if (enabled && provider.shouldTick() && this.providersTicking.contains(provider) == false) {
                this.providersTicking.add(provider);
            } else {
                this.providersTicking.remove(provider);
            }

            return true;
        }

        return false;
    }

    public void tickProviders(MinecraftServer server, int tickCounter, Profiler profiler) {
        if (this.providersTicking.isEmpty() == false) {
            for (IDataProvider provider : this.providersTicking) {
                if ((tickCounter % provider.getTickInterval()) == 0) {
                    provider.tick(server, tickCounter, profiler);
                }
            }
        }
    }

    protected void registerEnabledPacketHandlers() {
        for (IDataProvider provider : this.providersImmutable) {
            this.updatePacketHandlerRegistration(provider);
        }
    }

    protected void updatePacketHandlerRegistration(IDataProvider provider) {
        if (provider.isEnabled()) {
            provider.registerHandler();
        } else {
            provider.unregisterHandler();
        }
    }

    public void onCaptureImmutable(@Nonnull RegistryAccess.Frozen immutable) {
        this.immutable = immutable;
    }

    public RegistryAccess.Frozen getRegistryManager() {
        return this.immutable;
    }

    public void onServerTickEndPre() {
        for (IDataProvider provider : this.providersImmutable) {
            provider.onTickEndPre();
        }
    }

    public void onServerTickEndPost() {
        for (IDataProvider provider : this.providersImmutable) {
            provider.onTickEndPost();
        }
    }

    public Optional<IDataProvider> getProviderByName(String providerName) {
        return Optional.ofNullable(this.providers.get(providerName));
    }

    /* public @Nullable IServuxSetting<?> getSettingByName(String name)
    {
        if (name.contains(":"))
        {
            String[] parts = name.split(":");
            String providerName = parts[0];
            String settingName = parts[1];
            IDataProvider provider = this.providers.get(providerName);

            if (provider != null)
            {
                for (IServuxSetting<?> setting : provider.getSettings())
                {
                    if (setting.name().equalsIgnoreCase(settingName))
                    {
                        return setting;
                    }
                }
            }
        }
        else
        {
            for (IDataProvider provider : this.providersImmutable)
            {
                for (IServuxSetting<?> setting : provider.getSettings())
                {
                    if (setting.name().equalsIgnoreCase(name))
                    {
                        return setting;
                    }
                }
            }
        }
        return null;
    }
    */
    /*
    public void readFromConfig()
    {
        JsonElement el = JsonUtils.parseJsonFileAsPath(this.getConfigFile());
        JsonObject obj = null;

        Servux.debugLog("DataProviderManager#readFromConfig()");

        if (el != null && el.isJsonObject())
        {
            JsonObject root = el.getAsJsonObject();

            if (JsonUtils.hasObject(root, "DataProviderToggles"))
            {
                obj = JsonUtils.getNestedObject(root, "DataProviderToggles", false);
            }

            for (IDataProvider provider : this.providersImmutable)
            {
                String name = provider.getName();

                if (JsonUtils.hasObject(root, name))
                {
                    provider.fromJson(JsonUtils.getNestedObject(root, name, false));
                }
            }

            // If reading the config
            for (IDataProvider provider : this.providersImmutable)
            {
                if (obj != null)
                {
                    this.setProviderEnabled(provider, JsonUtils.getBooleanOrDefault(obj, provider.getName(), false));
                }
                else
                {
                    this.setProviderEnabled(provider, false);
                }

                // servux_main should never be disabled, because it provides the config management.
                if (provider.getName().equals("servux_main") && !provider.isEnabled())
                {
                    this.setProviderEnabled(provider, true);
                }
            }
        }
        else
        {
            // If writing a new config file (Disable the debug_data by default),
            // and then respect the config afterward.
            for (IDataProvider provider : this.providersImmutable)
            {
                this.setProviderEnabled(provider, !provider.getName().equals("debug_data"));
            }
        }
    } */

    /* public void writeToConfig()
    {
        JsonObject root = new JsonObject();
        JsonObject objToggles = new JsonObject();

        ServuxLitematicsProtocol.LOGGER.debug("DataProviderManager#writeToConfig()");

        for (IDataProvider provider : this.providersImmutable)
        {
            String name = provider.getName();
            objToggles.add(name, new JsonPrimitive(provider.isEnabled()));
        }

        root.add("DataProviderToggles", objToggles);

        for (IDataProvider provider : this.providersImmutable)
        {
            String name = provider.getName();
            root.add(name, provider.toJson());
        }

        JsonUtils.writeJsonToFileAsPath(root, this.getConfigFile());
    } */

    /* protected Path getConfigFile()
    {
        if (this.configDir == null)
        {
            this.configDir = Reference.DEFAULT_CONFIG_DIR;
        }

        if (!Files.exists(this.configDir))
        {
            try
            {
                Files.createDirectory(this.configDir);
            }
            catch (Exception err)
            {
                Servux.LOGGER.error("getConfigFile: Error creating config directory '{}'; {}", this.configDir.toAbsolutePath(), err.getMessage());
            }
        }

        return this.configDir.resolve("servux.json");
    } */
}
