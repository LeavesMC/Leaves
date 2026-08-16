// LeavesMC - Protocol support for Jade mod
// Copyright (c) 2024 LeavesMC
// Original source: https://github.com/snownee/Jade (MIT License)
// This file is licensed under the GNU General Public License v3.0.

package org.leavesmc.leaves.protocol.jade.provider;

import net.minecraft.resources.Identifier;

public interface JadeProvider {

    Identifier getUid();

    default int getDefaultPriority() {
        return 0;
    }
}
