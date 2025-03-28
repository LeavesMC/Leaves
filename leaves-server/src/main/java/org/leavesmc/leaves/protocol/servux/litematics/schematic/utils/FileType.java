package org.leavesmc.leaves.protocol.servux.litematics.schematic.utils;

import java.nio.file.Files;
import java.nio.file.Path;

public enum FileType {
    INVALID,
    UNKNOWN,
    JSON,
    LITEMATICA_SCHEMATIC,
    SCHEMATICA_SCHEMATIC,
    SPONGE_SCHEMATIC,
    VANILLA_STRUCTURE;

    public static FileType fromName(String fileName) {
        if (fileName.endsWith(".litematic")) {
            return LITEMATICA_SCHEMATIC;
        } else if (fileName.endsWith(".schematic")) {
            return SCHEMATICA_SCHEMATIC;
        } else if (fileName.endsWith(".nbt")) {
            return VANILLA_STRUCTURE;
        } else if (fileName.endsWith(".schem")) {
            return SPONGE_SCHEMATIC;
        } else if (fileName.endsWith(".json")) {
            return JSON;
        }

        return UNKNOWN;
    }

    public static FileType fromFile(Path file) {
        if (Files.exists(file) && Files.isReadable(file)) {
            return fromName(file.getFileName().toString());
        } else {
            return INVALID;
        }
    }

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

