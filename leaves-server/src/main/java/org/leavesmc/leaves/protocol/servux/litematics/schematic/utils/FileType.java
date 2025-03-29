package org.leavesmc.leaves.protocol.servux.litematics.schematic.utils;

public enum FileType {
    INVALID,
    UNKNOWN,
    JSON,
    LITEMATICA_SCHEMATIC,
    SCHEMATICA_SCHEMATIC,
    SPONGE_SCHEMATIC,
    VANILLA_STRUCTURE;

    public static String getString(FileType type) {
        return switch (type) {
            case LITEMATICA_SCHEMATIC -> "litematic";
            case SCHEMATICA_SCHEMATIC -> "schematic";
            case SPONGE_SCHEMATIC -> "sponge";
            case VANILLA_STRUCTURE -> "vanilla_nbt";
            case JSON -> "JSON";
            case INVALID -> "invalid";
            case UNKNOWN -> "unknown";
        };
    }
}

