package org.leavesmc.leaves.bot.agent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.agent.configs.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Configs<E> {

    private static final Map<String, Configs<?>> configs = new HashMap<>();

    public static final Configs<Boolean> SKIP_SLEEP = register(new SkipSleepConfig());
    public static final Configs<Boolean> ALWAYS_SEND_DATA = register(new AlwaysSendDataConfig());
    public static final Configs<Boolean> SPAWN_PHANTOM = register(new SpawnPhantomConfig());
    public static final Configs<Integer> SIMULATION_DISTANCE = register(new SimulationDistanceConfig());

    public final AbstractBotConfig<E> config;

    private Configs(AbstractBotConfig<E> config) {
        this.config = config;
    }

    @NotNull
    @Contract(pure = true)
    public static Collection<Configs<?>> getConfigs() {
        return configs.values();
    }

    @Nullable
    public static Configs<?> getConfig(String name) {
        return configs.get(name);
    }

    @NotNull
    private static <T> Configs<T> register(AbstractBotConfig<T> botConfig) {
        Configs<T> config = new Configs<>(botConfig);
        configs.put(botConfig.getName(), config);
        return config;
    }
}
