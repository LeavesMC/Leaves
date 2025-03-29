package org.leavesmc.leaves.protocol.servux;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServuxProtocol {

    public static final String PROTOCOL_ID = "servux";
    public static final Logger LOGGER = LoggerFactory.getLogger(PROTOCOL_ID.toUpperCase());
    public static final String SERVUX_STRING = ProtocolUtils.buildProtocolVersion(PROTOCOL_ID);

    @Contract("_ -> new")
    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(PROTOCOL_ID, path);
    }
}
