package org.leavesmc.leaves.bot.agent;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;

import java.util.List;
import java.util.function.Supplier;

public abstract class BotConfig<E> {

    private final String name;
    private final CommandArgument argument;
    private final Supplier<BotConfig<E>> creator;
    protected ServerBot bot;

    public BotConfig(String name, CommandArgument argument, Supplier<BotConfig<E>> creator) {
        this.name = name;
        this.argument = argument;
        this.creator = creator;
    }

    public BotConfig<E> setBot(ServerBot bot) {
        this.bot = bot;
        return this;
    }

    public abstract E getValue();

    public abstract void setValue(@NotNull CommandArgumentResult result) throws IllegalArgumentException;

    public List<String> getMessage() {
        return List.of(this.bot.getScoreboardName() + "'s " + this.getName() + ": " + this.getValue());
    }

    public List<String> getChangeMessage() {
        return List.of(this.bot.getScoreboardName() + "'s " + this.getName() + " changed: " + this.getValue());
    }

    public String getName() {
        return name;
    }

    public CommandArgument getArgument() {
        return argument;
    }

    @NotNull
    public BotConfig<E> create(ServerBot bot) {
        return this.creator.get().setBot(bot);
    }

    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        nbt.putString("configName", this.name);
        return nbt;
    }

    public abstract void load(@NotNull CompoundTag nbt);
}
