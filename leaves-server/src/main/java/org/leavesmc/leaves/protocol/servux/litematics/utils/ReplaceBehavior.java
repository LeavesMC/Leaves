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