package org.leavesmc.leaves.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.leavesmc.leaves.command.CommandNode.getNameForNode;

@SuppressWarnings({"ClassCanBeRecord", "unused"})
public class CommandContext {
    private final com.mojang.brigadier.context.CommandContext<CommandSourceStack> source;

    public CommandContext(com.mojang.brigadier.context.CommandContext<CommandSourceStack> source) {
        this.source = source;
    }

    public com.mojang.brigadier.context.CommandContext<CommandSourceStack> getChild() {
        return source.getChild();
    }

    public com.mojang.brigadier.context.CommandContext<CommandSourceStack> getLastChild() {
        return source.getLastChild();
    }

    public Command<CommandSourceStack> getCommand() {
        return source.getCommand();
    }

    public CommandSourceStack getSource() {
        return source.getSource();
    }

    public CommandSender getSender() {
        return source.getSource().getSender();
    }

    public <V> @NotNull V getArgument(final String name, final Class<V> clazz) {
        return source.getArgument(name, clazz);
    }

    public int getInteger(final String name) {
        return source.getArgument(name, Integer.class);
    }

    public boolean getBoolean(final String name) {
        return source.getArgument(name, Boolean.class);
    }

    public String getString(final String name) {
        return source.getArgument(name, String.class);
    }

    @SuppressWarnings("unchecked")
    public <V> @NotNull V getArgument(final Class<? extends ArgumentNode<V>> nodeClass) {
        String name = getNameForNode(nodeClass);
        return (V) source.getArgument(name, Object.class);
    }

    public <V> V getArgumentOrDefault(final Class<? extends ArgumentNode<V>> nodeClass, final V defaultValue) {
        try {
            return getArgument(nodeClass);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public <V> V getArgumentOrDefault(final String name, final Class<V> clazz, final V defaultValue) {
        try {
            return source.getArgument(name, clazz);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public String getStringOrDefault(final String name, final String defaultValue) {
        return getArgumentOrDefault(name, String.class, defaultValue);
    }

    public int getIntegerOrDefault(final String name, final int defaultValue) {
        return getArgumentOrDefault(name, Integer.class, defaultValue);
    }

    public float getFloatOrDefault(final String name, final float defaultValue) {
        return getArgumentOrDefault(name, Float.class, defaultValue);
    }

    public RedirectModifier<CommandSourceStack> getRedirectModifier() {
        return source.getRedirectModifier();
    }

    public StringRange getRange() {
        return source.getRange();
    }

    public String getInput() {
        return source.getInput();
    }

    public CommandNode<CommandSourceStack> getRootNode() {
        return source.getRootNode();
    }

    public List<ParsedCommandNode<CommandSourceStack>> getNodes() {
        return source.getNodes();
    }

    public boolean hasNodes() {
        return source.hasNodes();
    }

    public boolean isForked() {
        return source.isForked();
    }

    public com.mojang.brigadier.context.CommandContext<CommandSourceStack> getMojangContext() {
        return source;
    }
}
