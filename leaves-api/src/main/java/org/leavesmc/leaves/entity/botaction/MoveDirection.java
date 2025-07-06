package org.leavesmc.leaves.entity.botaction;

public enum MoveDirection {
    FORWARD("forward"),
    BACKWARD("backward"),
    LEFT("left"),
    RIGHT("right");

    public final String name;

    MoveDirection(String name) {
        this.name = name;
    }
}
