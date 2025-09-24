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

public interface LeavesCommandContext {

    com.mojang.brigadier.context.CommandContext<CommandSourceStack> getChild();

    com.mojang.brigadier.context.CommandContext<CommandSourceStack> getLastChild();

    Command<CommandSourceStack> getCommand();

    CommandSourceStack getSource();

    CommandSender getSender();

    <V> @NotNull V getArgument(final String name, final Class<V> clazz);

    int getInteger(final String name);

    boolean getBoolean(final String name);

    String getString(final String name);

    <V> V getArgumentOrDefault(final String name, final Class<V> clazz, final V defaultValue);

    String getStringOrDefault(final String name, final String defaultValue);

    int getIntegerOrDefault(final String name, final int defaultValue);

    float getFloatOrDefault(final String name, final float defaultValue);

    RedirectModifier<CommandSourceStack> getRedirectModifier();

    StringRange getRange();

    String getInput();

    CommandNode<CommandSourceStack> getRootNode();

    List<ParsedCommandNode<CommandSourceStack>> getNodes();

    boolean hasNodes();

    boolean isForked();

    com.mojang.brigadier.context.CommandContext<CommandSourceStack> rawContext();
}
