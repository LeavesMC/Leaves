package org.leavesmc.leaves.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class CommandNode {
    private static final Map<Class<? extends CommandNode>, String> class2NameMap = new HashMap<>();

    protected final String name;
    protected final List<CommandNode> children = new ArrayList<>();

    protected CommandNode(String name) {
        this.name = name;
        class2NameMap.put(getClass(), name);
    }

    @SafeVarargs
    protected final void children(Supplier<? extends CommandNode>... childrenClasses) {
        this.children.addAll(Stream.of(childrenClasses).map(Supplier::get).toList());
    }

    protected abstract ArgumentBuilder<CommandSourceStack, ?> compileBase();

    protected boolean execute(CommandContext context) throws CommandSyntaxException {
        return true;
    }

    public boolean requires(CommandSourceStack source) {
        return true;
    }

    public String getName() {
        return this.name;
    }

    protected ArgumentBuilder<CommandSourceStack, ?> compile() {
        ArgumentBuilder<CommandSourceStack, ?> builder = compileBase().requires(this::requires);

        for (CommandNode child : children) {
            builder = builder.then(child.compile());
        }

        if (canExecute()) {
            builder = builder.executes(mojangCtx -> {
                CommandContext ctx = new CommandContext(mojangCtx);
                return execute(ctx) ? 1 : 0;
            });
        }

        return builder;
    }

    protected boolean canExecute() {
        return isMethodOverridden("execute", CommandNode.class);
    }

    protected boolean isMethodOverridden(String methodName, @NotNull Class<?> baseClass) {
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method.getDeclaringClass() != baseClass;
            }
        }
        return false;
    }

    public static String getNameForNode(Class<? extends CommandNode> nodeClass) {
        return class2NameMap.get(nodeClass);
    }
}