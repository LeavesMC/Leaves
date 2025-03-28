package org.leavesmc.leaves.protocol.servux.litematics.schematic.utils;

public enum ReplaceBehavior {
    NONE("none", "litematica.gui.label.replace_behavior.none"),
    ALL("all", "litematica.gui.label.replace_behavior.all"),
    WITH_NON_AIR("with_non_air", "litematica.gui.label.replace_behavior.with_non_air");

    private final String configString;
    private final String translationKey;

    private ReplaceBehavior(String configString, String translationKey) {
        this.configString = configString;
        this.translationKey = translationKey;
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