package org.leavesmc.leaves.bot.agent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.agent.actions.AttackAction;
import org.leavesmc.leaves.bot.agent.actions.BreakBlockAction;
import org.leavesmc.leaves.bot.agent.actions.DropAction;
import org.leavesmc.leaves.bot.agent.actions.FishAction;
import org.leavesmc.leaves.bot.agent.actions.JumpAction;
import org.leavesmc.leaves.bot.agent.actions.LookAction;
import org.leavesmc.leaves.bot.agent.actions.RotateAction;
import org.leavesmc.leaves.bot.agent.actions.RotationAction;
import org.leavesmc.leaves.bot.agent.actions.ShootAction;
import org.leavesmc.leaves.bot.agent.actions.SneakAction;
import org.leavesmc.leaves.bot.agent.actions.SwimAction;
import org.leavesmc.leaves.bot.agent.actions.UseItemAction;
import org.leavesmc.leaves.bot.agent.actions.UseItemOffHandAction;
import org.leavesmc.leaves.bot.agent.actions.UseItemOnAction;
import org.leavesmc.leaves.bot.agent.actions.UseItemOnOffhandAction;
import org.leavesmc.leaves.bot.agent.actions.UseItemToAction;
import org.leavesmc.leaves.bot.agent.actions.UseItemToOffhandAction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Actions {

    private static final Map<String, AbstractBotAction<?>> actions = new HashMap<>();

    public static void registerAll() {
        register(new AttackAction());
        register(new BreakBlockAction());
        register(new DropAction());
        register(new JumpAction());
        register(new RotateAction());
        register(new SneakAction());
        register(new UseItemAction());
        register(new UseItemOnAction());
        register(new UseItemToAction());
        register(new LookAction());
        register(new FishAction());
        register(new SwimAction());
        register(new UseItemOffHandAction());
        register(new UseItemOnOffhandAction());
        register(new UseItemToOffhandAction());
        register(new RotationAction());
        register(new ShootAction());
    }

    public static boolean register(@NotNull AbstractBotAction<?> action) {
        if (!actions.containsKey(action.getName())) {
            actions.put(action.getName(), action);
            return true;
        }
        return false;
    }

    public static boolean unregister(@NotNull String name) {
        if (actions.containsKey(name)) {
            actions.remove(name);
            return true;
        }
        return false;
    }

    @NotNull
    @Contract(pure = true)
    public static Collection<AbstractBotAction<?>> getAll() {
        return actions.values();
    }

    @NotNull
    public static Set<String> getNames() {
        return actions.keySet();
    }

    @Nullable
    public static AbstractBotAction<?> getForName(String name) {
        return actions.get(name);
    }
}
