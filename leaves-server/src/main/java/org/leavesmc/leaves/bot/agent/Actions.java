package org.leavesmc.leaves.bot.agent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.agent.actions.CraftAttackAction;
import org.leavesmc.leaves.bot.agent.actions.CraftBotAction;
import org.leavesmc.leaves.bot.agent.actions.CraftBreakBlockAction;
import org.leavesmc.leaves.bot.agent.actions.CraftDropAction;
import org.leavesmc.leaves.bot.agent.actions.CraftFishAction;
import org.leavesmc.leaves.bot.agent.actions.CraftJumpAction;
import org.leavesmc.leaves.bot.agent.actions.CraftLookAction;
import org.leavesmc.leaves.bot.agent.actions.CraftMoveAction;
import org.leavesmc.leaves.bot.agent.actions.CraftRotationAction;
import org.leavesmc.leaves.bot.agent.actions.CraftShootAction;
import org.leavesmc.leaves.bot.agent.actions.CraftSneakAction;
import org.leavesmc.leaves.bot.agent.actions.CraftSwimAction;
import org.leavesmc.leaves.bot.agent.actions.CraftUseItemAction;
import org.leavesmc.leaves.bot.agent.actions.CraftUseItemAutoAction;
import org.leavesmc.leaves.bot.agent.actions.CraftUseItemAutoOffhandAction;
import org.leavesmc.leaves.bot.agent.actions.CraftUseItemOffHandAction;
import org.leavesmc.leaves.bot.agent.actions.CraftUseItemOnAction;
import org.leavesmc.leaves.bot.agent.actions.CraftUseItemOnOffhandAction;
import org.leavesmc.leaves.bot.agent.actions.CraftUseItemToAction;
import org.leavesmc.leaves.bot.agent.actions.CraftUseItemToOffhandAction;
import org.leavesmc.leaves.entity.bot.action.BotAction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Actions {

    private static final Map<String, CraftBotAction<?>> actionsByName = new HashMap<>();
    private static final Map<Class<?>, CraftBotAction<?>> actionsByClass = new HashMap<>();

    public static void registerAll() {
        register(new CraftAttackAction());
        register(new CraftBreakBlockAction());
        register(new CraftDropAction());
        register(new CraftJumpAction());
        register(new CraftSneakAction());
        register(new CraftUseItemAction());
        register(new CraftUseItemOnAction());
        register(new CraftUseItemToAction());
        register(new CraftUseItemAutoAction());
        register(new CraftUseItemOffHandAction());
        register(new CraftUseItemOnOffhandAction());
        register(new CraftUseItemToOffhandAction());
        register(new CraftUseItemAutoOffhandAction());
        register(new CraftLookAction());
        register(new CraftFishAction());
        register(new CraftSwimAction());
        register(new CraftRotationAction());
        register(new CraftShootAction());
        register(new CraftMoveAction());
    }

    public static boolean register(@NotNull CraftBotAction<?> action) {
        if (!actionsByName.containsKey(action.getName())) {
            actionsByName.put(action.getName(), action);
            Class<?> interfaceClass = action.getInterfaceClass();
            actionsByClass.put(Objects.requireNonNullElseGet(interfaceClass, action::getClass), action);
            return true;
        }
        return false;
    }

    public static boolean unregister(@NotNull String name) {
        if (actionsByName.containsKey(name)) {
            CraftBotAction<?> action = actionsByName.get(name);
            Class<?> interfaceClass = action.getInterfaceClass();
            actionsByClass.remove(Objects.requireNonNullElseGet(interfaceClass, action::getClass));
            actionsByName.remove(name);
            return true;
        }
        return false;
    }

    @NotNull
    @Contract(pure = true)
    public static Collection<CraftBotAction<?>> getAll() {
        return actionsByName.values();
    }

    @NotNull
    public static Set<String> getNames() {
        return actionsByName.keySet();
    }

    @Nullable
    public static CraftBotAction<?> getForName(String name) {
        return actionsByName.get(name);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends BotAction<T>> T getForClass(@NotNull Class<T> type) {
        return (T) actionsByClass.get(type);
    }
}
