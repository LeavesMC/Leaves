// LeavesMC - Protocol support for Servux mod
// Copyright (c) 2024 LeavesMC
// Original source: https://github.com/RelativityMC/servux (LGPL-3.0 License)
// This file is licensed under the GNU General Public License v3.0.

package org.leavesmc.leaves.protocol.servux.litematics.utils;

public enum ReplaceBehavior {
    NONE("none"),
    ALL("all"),
    WITH_NON_AIR("with_non_air");

    private final String configString;

    ReplaceBehavior(String configString) {
        this.configString = configString;
    }

    public static ReplaceBehavior fromStringStatic(String name) {
        for (ReplaceBehavior val : ReplaceBehavior.values()) {
            if (val.configString.equalsIgnoreCase(name)) {
                return val;
            }
        }

        return ReplaceBehavior.NONE;
    }
}