package org.leavesmc.leaves.entity.bot.action.custom;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface CommandProcessor {

    @ApiStatus.Internal
    CommandProcessor DUMMY_PROCESSOR = new CommandProcessor() {

    };
}
