package org.leavesmc.leaves.bot.agent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.configs.AlwaysSendDataConfig;
import org.leavesmc.leaves.bot.agent.configs.LocatorBarConfig;
import org.leavesmc.leaves.bot.agent.configs.SimulationDistanceConfig;
import org.leavesmc.leaves.bot.agent.configs.SkipSleepConfig;
import org.leavesmc.leaves.bot.agent.configs.SpawnPhantomConfig;
import org.leavesmc.leaves.bot.agent.configs.TickTypeConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class Configs<E> {

    private static final Map<String, Configs<?>> configs = new HashMap<>();

    public static final Configs<Boolean> SKIP_SLEEP = register(SkipSleepConfig.class, SkipSleepConfig::new);
    public static final Configs<Boolean> ALWAYS_SEND_DATA = register(AlwaysSendDataConfig.class, AlwaysSendDataConfig::new);
    public static final Configs<Boolean> SPAWN_PHANTOM = register(SpawnPhantomConfig.class, SpawnPhantomConfig::new);
    public static final Configs<Integer> SIMULATION_DISTANCE = register(SimulationDistanceConfig.class, SimulationDistanceConfig::new);
    public static final Configs<ServerBot.TickType> TICK_TYPE = register(TickTypeConfig.class, TickTypeConfig::new);
    public static final Configs<Boolean> ENABLE_LOCATOR_BAR = register(LocatorBarConfig.class, LocatorBarConfig::new);

    private final Class<? extends AbstractBotConfig<E>> configClass;
    private final Supplier<? extends AbstractBotConfig<E>> configCreator;

    private Configs(Class<? extends AbstractBotConfig<E>> configClass, Supplier<? extends AbstractBotConfig<E>> configCreator) {
        this.configClass = configClass;
        this.configCreator = configCreator;
    }

    public Class<? extends AbstractBotConfig<E>> getConfigClass() {
        return configClass;
    }

    public AbstractBotConfig<E> createConfig(ServerBot bot) {
        return configCreator.get().setBot(bot);
    }

    @Nullable
    public static Configs<?> getConfig(String name) {
        return configs.get(name);
    }

    @NotNull
    @Contract(pure = true)
    public static Collection<Configs<?>> getConfigs() {
        return configs.values();
    }

    @NotNull
    @Contract(pure = true)
    public static Collection<String> getConfigNames() {
        return configs.keySet();
    }

    @NotNull
    private static <E> Configs<E> register(Class<? extends AbstractBotConfig<E>> configClass, Supplier<? extends AbstractBotConfig<E>> configCreator) {
        Configs<E> config = new Configs<>(configClass, configCreator);
        configs.put(config.createConfig(null).getName(), config);
        return config;
    }
}
