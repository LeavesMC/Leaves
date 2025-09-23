package org.leavesmc.leaves.bot.agent.configs;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.ExtraData;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.WrappedArgument;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.Supplier;

public abstract class AbstractBotConfig<Value, Type, E extends AbstractBotConfig<Value, Type, E>> {
    private final String name;
    private final WrappedArgument<Type> argument;
    private final Supplier<E> creator;

    protected ServerBot bot;

    public AbstractBotConfig(String name, ArgumentType<Type> type, Supplier<E> creator) {
        this.name = name;
        this.argument = new WrappedArgument<>(name, type);
        if (shouldApplySuggestions()) {
            this.argument.suggests(this::applySuggestions);
        }
        this.creator = creator;
    }

    @SuppressWarnings("RedundantThrows")
    public void applySuggestions(final CommandContext context, final SuggestionsBuilder builder) throws CommandSyntaxException {
    }

    public AbstractBotConfig<Value, Type, E> setBot(ServerBot bot) {
        this.bot = bot;
        return this;
    }

    public E create() {
        return creator.get();
    }

    public abstract Value getValue();

    public abstract void setValue(Value value) throws CommandSyntaxException;

    public abstract Value loadFromCommand(@NotNull CommandContext context) throws CommandSyntaxException;

    public String getName() {
        return name;
    }

    public Component getNameComponent() {
        return Component.text(getName(), NamedTextColor.AQUA).hoverEvent(HoverEvent.showText(Component.text(getExtraDataString())));
    }

    public String getExtraDataString() {
        return getExtraDataString(new ExtraData(new ArrayList<>()));
    }

    public String getExtraDataString(@NotNull ExtraData data) {
        return data.raw().stream()
            .map(pair -> pair.getLeft() + "=" + pair.getRight())
            .reduce((a, b) -> a + ", " + b)
            .orElse("No data");
    }

    public WrappedArgument<Type> getArgument() {
        return argument;
    }

    public ServerBot getBot() {
        return bot;
    }

    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        nbt.putString("configName", this.name);
        return nbt;
    }

    public abstract void load(@NotNull CompoundTag nbt);

    private boolean shouldApplySuggestions() {
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.getName().equals("applySuggestions")) {
                return method.getDeclaringClass() != AbstractBotConfig.class;
            }
        }
        return false;
    }
}
