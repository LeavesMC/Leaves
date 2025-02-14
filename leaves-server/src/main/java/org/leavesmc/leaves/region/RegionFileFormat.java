package org.leavesmc.leaves.region;

public enum RegionFileFormat {
    ANVIL("mca"),
    LINEAR("linear");

    private final String argument;

    RegionFileFormat(String argument) {
        this.argument = argument;
    }

    public String getArgument() {
        return argument;
    }
}
