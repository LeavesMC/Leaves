// LeavesMC - Protocol support for Servux mod
// Copyright (c) 2024 LeavesMC
// Original source: https://github.com/RelativityMC/servux (LGPL-3.0 License)
// This file is licensed under the GNU General Public License v3.0.

package org.leavesmc.leaves.protocol.servux;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServuxProtocol {

    public static final String PROTOCOL_ID = "servux";
    public static final Logger LOGGER = LoggerFactory.getLogger(PROTOCOL_ID.toUpperCase());
    public static final String SERVUX_STRING = ProtocolUtils.buildProtocolVersion(PROTOCOL_ID);

    @Contract("_ -> new")
    public static Identifier id(String path) {
        return Identifier.tryBuild(PROTOCOL_ID, path);
    }
}
