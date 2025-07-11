package org.leavesmc.leaves.bot.agent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.agent.actions.ServerAttackAction;
import org.leavesmc.leaves.bot.agent.actions.ServerBotAction;
import org.leavesmc.leaves.bot.agent.actions.ServerBreakBlockAction;
import org.leavesmc.leaves.bot.agent.actions.ServerDropAction;
import org.leavesmc.leaves.bot.agent.actions.ServerFishAction;
import org.leavesmc.leaves.bot.agent.actions.ServerJumpAction;
import org.leavesmc.leaves.bot.agent.actions.ServerLookAction;
import org.leavesmc.leaves.bot.agent.actions.ServerMoveAction;
import org.leavesmc.leaves.bot.agent.actions.ServerRotationAction;
import org.leavesmc.leaves.bot.agent.actions.ServerShootAction;
import org.leavesmc.leaves.bot.agent.actions.ServerSneakAction;
import org.leavesmc.leaves.bot.agent.actions.ServerSwimAction;
import org.leavesmc.leaves.bot.agent.actions.ServerUseItemAction;
import org.leavesmc.leaves.bot.agent.actions.ServerUseItemAutoAction;
import org.leavesmc.leaves.bot.agent.actions.ServerUseItemAutoOffhandAction;
import org.leavesmc.leaves.bot.agent.actions.ServerUseItemOffhandAction;
import org.leavesmc.leaves.bot.agent.actions.ServerUseItemOnAction;
import org.leavesmc.leaves.bot.agent.actions.ServerUseItemOnOffhandAction;
import org.leavesmc.leaves.bot.agent.actions.ServerUseItemToAction;
import org.leavesmc.leaves.bot.agent.actions.ServerUseItemToOffhandAction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Actions {

    private static final Map<String, ServerBotAction<?>> actionsByName = new HashMap<>();
    private static final Map<Class<?>, ServerBotAction<?>> actionsByClass = new HashMap<>();

    public static void registerAll() {
        register(new ServerAttackAction());
        register(new ServerBreakBlockAction());
        register(new ServerDropAction());
        register(new ServerJumpAction());
        register(new ServerSneakAction());
        register(new ServerUseItemAction());
        register(new ServerUseItemOnAction());
        register(new ServerUseItemToAction());
        register(new ServerUseItemAutoAction());
        register(new ServerUseItemOffhandAction());
        register(new ServerUseItemOnOffhandAction());
        register(new ServerUseItemToOffhandAction());
        register(new ServerUseItemAutoOffhandAction());
        register(new ServerLookAction());
        register(new ServerFishAction());
        register(new ServerSwimAction());
        register(new ServerRotationAction());
        register(new ServerShootAction());
        register(new ServerMoveAction());
    }

    public static boolean register(@NotNull ServerBotAction<?> action) {
        if (!actionsByName.containsKey(action.getName())) {
            actionsByName.put(action.getName(), action);
            actionsByClass.put(action.getActionClass(), action);
            return true;
        }
        return false;
    }

    public static boolean unregister(@NotNull String name) {
        if (actionsByName.containsKey(name)) {
            actionsByClass.remove(actionsByName.get(name).getActionClass());
            actionsByName.remove(name);
            return true;
        }
        return false;
    }

    @NotNull
    @Contract(pure = true)
    public static Collection<ServerBotAction<?>> getAll() {
        return actionsByName.values();
    }

    @NotNull
    public static Set<String> getNames() {
        return actionsByName.keySet();
    }

    @Nullable
    public static ServerBotAction<?> getForName(String name) {
        return actionsByName.get(name);
    }

    @Nullable
    public static ServerBotAction<?> getForClass(@NotNull Class<?> type) {
        return actionsByClass.get(type);
    }
}
