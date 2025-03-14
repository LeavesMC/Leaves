package org.leavesmc.leaves.bot.agent;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;

import java.util.List;

public abstract class AbstractBotConfig<E> {

    private final String name;
    private final CommandArgument argument;

    protected ServerBot bot;

    public AbstractBotConfig(String name, CommandArgument argument) {
        this.name = name;
        this.argument = argument;
    }

    public AbstractBotConfig<E> setBot(ServerBot bot) {
        this.bot = bot;
        return this;
    }

    public abstract E getValue();

    public abstract void setValue(E value) throws IllegalArgumentException;

    @SuppressWarnings("unchecked")
    public void setFromCommand(@NotNull CommandArgumentResult result) throws IllegalArgumentException {
        if (argument == CommandArgument.EMPTY) {
            throw new IllegalArgumentException("No argument for " + this.getName());
        }
        try {
            this.setValue((E) result.read(argument.getArgumentTypes().getFirst().getType()));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid argument type for " + this.getName() + ": " + e.getMessage());
        }
    }

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
    public CompoundTag save(@NotNull CompoundTag nbt) {
        nbt.putString("configName", this.name);
        return nbt;
    }

    public abstract void load(@NotNull CompoundTag nbt);
}
