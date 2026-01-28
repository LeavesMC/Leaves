package org.leavesmc.leaves.bot.agent;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.entity.bot.action.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class Actions<T extends AbstractBotAction<T>> {

    private static final Map<String, Actions<? extends AbstractBotAction<?>>> actionsByName = new HashMap<>();
    private static final Map<Class<? extends BotAction<?>>, Actions<? extends AbstractBotAction<?>>> actionsByClass = new HashMap<>();

    static {
        register(AttackAction.class, ServerAttackAction::new);
        register(BreakBlockAction.class, ServerBreakBlockAction::new);
        register(DropAction.class, ServerDropAction::new);
        register(JumpAction.class, ServerJumpAction::new);
        register(SneakAction.class, ServerSneakAction::new);
        register(UseItemAutoAction.class, ServerUseItemAutoAction::new);
        register(UseItemAction.class, ServerUseItemAction::new);
        register(UseItemOnAction.class, ServerUseItemOnAction::new);
        register(UseItemToAction.class, ServerUseItemToAction::new);
        register(UseItemOffhandAction.class, ServerUseItemOffhandAction::new);
        register(UseItemOnOffhandAction.class, ServerUseItemOnOffhandAction::new);
        register(UseItemToOffhandAction.class, ServerUseItemToOffhandAction::new);
        register(LookAction.class, ServerLookAction::new);
        register(FishAction.class, ServerFishAction::new);
        register(SwimAction.class, ServerSwimAction::new);
        register(RotationAction.class, ServerRotationAction::new);
        register(MoveAction.class, ServerMoveAction::new);
        register(MountAction.class, ServerMountAction::new);
        register(SwapAction.class, ServerSwapAction::new);
    }

    public static <T extends AbstractBotAction<T>> boolean register(Class<? extends BotAction<?>> apiType, @NotNull Supplier<T> creator) {
        AbstractBotAction<T> action = creator.get();
        if (!actionsByName.containsKey(action.getName())) {
            Actions<?> actions = new Actions<>(creator.get().getName(), apiType, creator);
            actionsByName.put(action.getName(), actions);
            actionsByClass.put(apiType, actions);
            return true;
        }
        return false;
    }

    private final String name;
    private final Class<?> apiType;
    private final Supplier<T> creator;

    private Actions(String name, Class<? extends BotAction<?>> apiType, Supplier<T> creator) {
        this.name = name;
        this.apiType = apiType;
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return apiType;
    }

    public T create() {
        return creator.get();
    }

    public T createAndLoad(CommandContext context) throws CommandSyntaxException {
        T action = create();
        action.loadCommand(context);
        return action;
    }

    public static boolean unregister(@NotNull String name) {
        Actions<?> action = actionsByName.remove(name);
        if (action != null) {
            actionsByClass.remove(action.getClass());
            return true;
        }
        return false;
    }

    @NotNull
    @Contract(pure = true)
    public static Collection<Actions<? extends AbstractBotAction<?>>> getAll() {
        return actionsByName.values();
    }

    @NotNull
    public static Set<String> getNames() {
        return actionsByName.keySet();
    }

    @Nullable
    public static Actions<?> getByName(String name) {
        return actionsByName.get(name);
    }

    @Nullable
    public static Actions<?> getByClass(@NotNull Class<?> type) {
        return actionsByClass.get(type);
    }
}