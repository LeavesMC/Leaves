// LeavesMC - Protocol support for Jade mod
// Copyright (c) 2024 LeavesMC
// Original source: https://github.com/snownee/Jade (MIT License)
// This file is licensed under the GNU General Public License v3.0.

package org.leavesmc.leaves.protocol.jade.provider;

import net.minecraft.nbt.CompoundTag;
import org.leavesmc.leaves.protocol.jade.accessor.Accessor;

public interface ServerDataProvider<T extends Accessor<?>> extends JadeProvider {
    void appendServerData(CompoundTag data, T accessor);
}
