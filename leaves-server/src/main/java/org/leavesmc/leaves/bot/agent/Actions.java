package org.leavesmc.leaves.bot.agent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.entity.bot.action.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Actions {

    private static final Map<String, ServerBotAction<?>> actionsByName = new HashMap<>();
    private static final Map<Class<?>, ServerBotAction<?>> actionsByClass = new HashMap<>();

    public static void registerAll() {
        register(new ServerAttackAction(), AttackAction.class);
        register(new ServerBreakBlockAction(), BreakBlockAction.class);
        register(new ServerDropAction(), DropAction.class);
        register(new ServerJumpAction(), JumpAction.class);
        register(new ServerSneakAction(), SneakAction.class);
        register(new ServerUseItemAutoAction(), UseItemAutoAction.class);
        register(new ServerUseItemAction(), UseItemAction.class);
        register(new ServerUseItemOnAction(), UseItemOnAction.class);
        register(new ServerUseItemToAction(), UseItemToAction.class);
        register(new ServerUseItemOffhandAction(), UseItemOffhandAction.class);
        register(new ServerUseItemOnOffhandAction(), UseItemOnOffhandAction.class);
        register(new ServerUseItemToOffhandAction(), UseItemToOffhandAction.class);
        register(new ServerLookAction(), LookAction.class);
        register(new ServerFishAction(), FishAction.class);
        register(new ServerSwimAction(), SwimAction.class);
        register(new ServerRotationAction(), RotationAction.class);
        register(new ServerMoveAction(), MoveAction.class);
        register(new ServerMountAction(), MountAction.class);
        register(new ServerSwapAction(), SwapAction.class);
    }

    public static boolean register(@NotNull ServerBotAction<?> action, Class<? extends BotAction<?>> type) {
        if (!actionsByName.containsKey(action.getName())) {
            actionsByName.put(action.getName(), action);
            actionsByClass.put(type, action);
            return true;
        }
        return false;
    }

    public static boolean unregister(@NotNull String name) {
        // TODO add in custom action api
        return true;
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
