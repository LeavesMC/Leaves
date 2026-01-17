package org.leavesmc.leaves.bot.agent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.configs.AbstractBotConfig;
import org.leavesmc.leaves.bot.agent.configs.AlwaysSendDataConfig;
import org.leavesmc.leaves.bot.agent.configs.LocatorBarConfig;
import org.leavesmc.leaves.bot.agent.configs.SimulationDistanceConfig;
import org.leavesmc.leaves.bot.agent.configs.SkipSleepConfig;
import org.leavesmc.leaves.bot.agent.configs.SpawnPhantomConfig;
import org.leavesmc.leaves.bot.agent.configs.TickTypeConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class Configs<E extends AbstractBotConfig<?>> {

    private static final Map<String, Configs<? extends AbstractBotConfig<?>>> configs = new HashMap<>();

    public static final Configs<SkipSleepConfig> SKIP_SLEEP = register(SkipSleepConfig.class, SkipSleepConfig::new);
    public static final Configs<AlwaysSendDataConfig> ALWAYS_SEND_DATA = register(AlwaysSendDataConfig.class, AlwaysSendDataConfig::new);
    public static final Configs<SpawnPhantomConfig> SPAWN_PHANTOM = register(SpawnPhantomConfig.class, SpawnPhantomConfig::new);
    public static final Configs<SimulationDistanceConfig> SIMULATION_DISTANCE = register(SimulationDistanceConfig.class, SimulationDistanceConfig::new);
    public static final Configs<TickTypeConfig> TICK_TYPE = register(TickTypeConfig.class, TickTypeConfig::new);
    public static final Configs<LocatorBarConfig> ENABLE_LOCATOR_BAR = register(LocatorBarConfig.class, LocatorBarConfig::new);

    private final Class<? extends AbstractBotConfig<?>> configClass;
    private final Supplier<? extends AbstractBotConfig<?>> configCreator;
    private final String name;

    private Configs(Class<? extends AbstractBotConfig<?>> configClass, Supplier<? extends AbstractBotConfig<?>> configCreator, String name) {
        this.configClass = configClass;
        this.configCreator = configCreator;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public E create() {
        return (E) configCreator.get();
    }

    @SuppressWarnings("unchecked")
    public E create(ServerBot bot) {
        E config = (E) configCreator.get();
        config.setBot(bot);
        return config;
    }

    @NotNull
    public static Optional<Configs<?>> getConfig(String name) {
        return configs.values().stream()
            .filter(config -> config.name.equals(name))
            .findFirst();
    }

    @NotNull
    public static <T> Optional<Configs<?>> getConfig(Class<? extends AbstractBotConfig<T>> configClass) {
        return configs.values().stream()
            .filter(config -> config.configClass.equals(configClass))
            .findFirst();
    }

    @NotNull
    @Contract(pure = true)
    public static Collection<Configs<? extends AbstractBotConfig<?>>> getConfigs() {
        return configs.values();
    }

    private static <T, E extends AbstractBotConfig<T>> @NotNull Configs<E> register(Class<? extends AbstractBotConfig<T>> configClass, Supplier<AbstractBotConfig<T>> configCreator) {
        String name = configCreator.get().getName();
        Configs<E> config = new Configs<>(configClass, configCreator, name);
        configs.put(name, config);
        return config;
    }
}
