// LeavesMC - Protocol support for Jade mod
// Copyright (c) 2024 LeavesMC
// Original source: https://github.com/snownee/Jade (MIT License)
// This file is licensed under the GNU General Public License v3.0.

package org.leavesmc.leaves.protocol.jade.provider;

import org.leavesmc.leaves.protocol.jade.accessor.Accessor;
import org.leavesmc.leaves.protocol.jade.util.ViewGroup;

import java.util.List;

public interface ServerExtensionProvider<T> extends JadeProvider {
    List<ViewGroup<T>> getGroups(Accessor<?> request);
}