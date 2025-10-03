package org.leavesmc.leaves.entity.bot.action.custom;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.leavesmc.leaves.entity.bot.Bot;

public abstract class CustomActionProvider {

    private CommandProcessor processor = CommandProcessor.DUMMY_PROCESSOR;

    public abstract String id();

    public abstract Plugin provider();

    public abstract boolean doTick(Bot bot, CustomAction action);

    public void withProcessor(CommandProcessor processor) {
        this.processor = processor;
    }

    @ApiStatus.Internal
    public CommandProcessor processor() {
        return processor;
    }
}
