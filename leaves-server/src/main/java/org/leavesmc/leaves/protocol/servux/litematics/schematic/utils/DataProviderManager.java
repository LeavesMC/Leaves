package org.leavesmc.leaves.protocol.servux.litematics.schematic.utils;

import net.minecraft.core.RegistryAccess;

import java.util.HashMap;

public class DataProviderManager {
    public static final DataProviderManager INSTANCE = new DataProviderManager();
    protected final HashMap<String, IDataProvider> providers = new HashMap<>();
    protected final RegistryAccess.Frozen immutable = RegistryAccess.EMPTY;
    public RegistryAccess.Frozen getRegistryManager() {
        return this.immutable;
    }
}
