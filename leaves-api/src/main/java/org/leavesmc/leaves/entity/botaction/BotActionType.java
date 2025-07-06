package org.leavesmc.leaves.entity.botaction;

/**
 * A Leaves bot action enum
 */

@org.jetbrains.annotations.ApiStatus.Experimental
public enum BotActionType {
    ATTACK("attack"),
    BREAK("break"),
    DROP("drop"),
    FISH("fish"),
    JUMP("jump"),
    LOOK("look"),
    ROTATE("rotate"),
    ROTATION("rotation"),
    SNEAK("sneak"),
    SWIM("swim"),
    USE("use"),
    USE_ON("use_on"),
    USE_TO("use_to"),
    USE_AUTO("use_auto"),
    USE_OFFHAND("use_offhand"),
    USE_ON_OFFHAND("use_on_offhand"),
    USE_TO_OFFHAND("use_to_offhand"),
    USE_AUTO_OFFHAND("use_auto_offhand");

    private final String name;

    BotActionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
