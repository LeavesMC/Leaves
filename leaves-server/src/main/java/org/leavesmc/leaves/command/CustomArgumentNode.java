package org.leavesmc.leaves.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CustomArgumentNode<T, B> extends ArgumentNode<B> {
    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends CustomArgumentNode>, CustomArgumentType<?, ?>> TYPES = new HashMap<>();

    protected CustomArgumentNode(String name, @NotNull CustomArgumentType<T, B> argumentType) {
        super(name, argumentType.getBaseArgumentType());
        TYPES.put(getClass(), argumentType);
    }

    public static <T, B> T transform(Class<? extends CustomArgumentNode<T, B>> nodeClass, B base) throws CommandSyntaxException {
        @SuppressWarnings("unchecked")
        CustomArgumentType<T, B> type = (CustomArgumentType<T, B>) TYPES.get(nodeClass);
        if (type == null) {
            throw new IllegalArgumentException("No custom argument type registered for " + nodeClass.getName());
        }
        return type.transform(base);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ArgumentBuilder<CommandSourceStack, ?> compileBase() {
        RequiredArgumentBuilder<CommandSourceStack, ?> argumentBuilder = (RequiredArgumentBuilder<CommandSourceStack, ?>) super.compileBase();
        CustomArgumentType<T, B> customArgumentType = (CustomArgumentType<T, B>) TYPES.get(getClass());
        argumentBuilder.suggests(
            (context, builder) -> customArgumentType.getSuggestions(new CommandContext(context), builder)
        );
        return argumentBuilder;
    }
}
