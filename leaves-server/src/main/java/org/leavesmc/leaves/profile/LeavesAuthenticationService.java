package org.leavesmc.leaves.profile;

import com.destroystokyo.paper.profile.PaperAuthenticationService;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import java.net.Proxy;

public class LeavesAuthenticationService extends PaperAuthenticationService {

    public LeavesAuthenticationService(Proxy proxy) {
        super(proxy);
    }

    @Override
    public MinecraftSessionService createMinecraftSessionService() {
        return new LeavesMinecraftSessionService(this.getServicesKeySet(), this.getProxy(), this.environment);
    }
}
