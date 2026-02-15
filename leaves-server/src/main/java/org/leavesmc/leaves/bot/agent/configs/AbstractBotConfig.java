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

public abstract class AbstractBotConfig<T> {

    private final String name;
    private final WrappedArgument<T> argument;

    protected ServerBot bot;

    public AbstractBotConfig(String name, ArgumentType<T> type) {
        this.name = name;
        this.argument = new WrappedArgument<>(name, type);
        if (shouldApplySuggestions()) {
            this.argument.suggests(this::applySuggestions);
        }
    }

    @SuppressWarnings("RedundantThrows")
    public void applySuggestions(final CommandContext context, final SuggestionsBuilder builder) throws CommandSyntaxException {
    }

    public AbstractBotConfig<T> setBot(ServerBot bot) {
        this.bot = bot;
        return this;
    }

    public abstract T getValue();

    public abstract void setValue(T value) throws CommandSyntaxException;

    public abstract T loadFromCommand(@NotNull CommandContext context) throws CommandSyntaxException;

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

    public WrappedArgument<T> getArgument() {
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