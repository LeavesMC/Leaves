package org.leavesmc.leaves.bot.agent.configs;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.neo_command.CommandContext;
import org.leavesmc.leaves.neo_command.WrappedArgument;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;

public abstract class AbstractBotConfig<O, I, E extends AbstractBotConfig<O, I, E>> {
    private final String name;
    private final WrappedArgument<I> argument;
    private final Supplier<E> creator;

    protected ServerBot bot;

    public AbstractBotConfig(String name, ArgumentType<I> type, Supplier<E> creator) {
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

    public AbstractBotConfig<O, I, E> setBot(ServerBot bot) {
        this.bot = bot;
        return this;
    }

    public E create() {
        return creator.get();
    }

    public abstract O getValue();

    public abstract void setValue(O value) throws CommandSyntaxException;

    public abstract O loadFromCommand(@NotNull CommandContext context) throws CommandSyntaxException;

    public List<Pair<String, String>> getExtraData() {
        return List.of();
    }

    public String getName() {
        return name;
    }

    public Component getNameComponent() {
        Component result = text(getName(), AQUA);
        if (!getExtraData().isEmpty()) {
            result = result.hoverEvent(showText(
                getExtraData().stream()
                    .map(pair -> text(pair.getKey() + "=" + pair.getValue()))
                    .reduce((a, b) -> a.append(text(", ")).append(b))
                    .orElseGet(() -> text(""))
            ));
        }
        return result;
    }

    public WrappedArgument<I> getArgument() {
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
