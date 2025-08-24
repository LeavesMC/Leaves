package org.leavesmc.leaves.bot.agent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

@SuppressWarnings({"unused"})
public class Configs {
    private static final Map<Class<?>, AbstractBotConfig<?, ?, ?>> configs = new HashMap<>();

    public static final SkipSleepConfig SKIP_SLEEP = register(new SkipSleepConfig());
    public static final AlwaysSendDataConfig ALWAYS_SEND_DATA = register(new AlwaysSendDataConfig());
    public static final SpawnPhantomConfig SPAWN_PHANTOM = register(new SpawnPhantomConfig());
    public static final SimulationDistanceConfig SIMULATION_DISTANCE = register(new SimulationDistanceConfig());
    public static final TickTypeConfig TICK_TYPE = register(new TickTypeConfig());
    public static final LocatorBarConfig ENABLE_LOCATOR_BAR = register(new LocatorBarConfig());

    @Nullable
    public static AbstractBotConfig<?, ?, ?> getConfig(String name) {
        return configs.values().stream()
            .filter(config -> config.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    @NotNull
    @Contract(pure = true)
    public static Collection<AbstractBotConfig<?, ?, ?>> getConfigs() {
        return configs.values();
    }

    @SuppressWarnings("unchecked")
    private static <O, I, E extends AbstractBotConfig<O, I, E>> @NotNull E register(AbstractBotConfig<O, I, E> instance) {
        configs.put(instance.getClass(), instance);
        return (E) instance;
    }
}
