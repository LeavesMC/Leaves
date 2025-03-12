package org.leavesmc.leaves.entity;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.entity.botaction.CustomBotAction;

import java.util.Collection;
import java.util.UUID;

/**
 * Simple fakeplayer manager
 */
public interface BotManager {

    /**
     * Gets a fakeplayer object by the given uuid.
     *
     * @param uuid the uuid to look up
     * @return a fakeplayer if one was found, null otherwise
     */
    @Nullable
    public Bot getBot(@NotNull UUID uuid);

    /**
     * Gets a fakeplayer object by the given name.
     *
     * @param name the name to look up
     * @return a fakeplayer if one was found, null otherwise
     */
    @Nullable
    public Bot getBot(@NotNull String name);

    /**
     * Gets a view of all currently logged in fakeplayers. This view is a reused object, making some operations like Collection.size() zero-allocation.
     *
     * @return a view of fakeplayers.
     */
    public Collection<Bot> getBots();

    /**
     * Register a custom bot action.
     *
     * @param name   action name
     * @param action action executor
     * @return true if success, or false
     */
    public boolean registerCustomBotAction(String name, CustomBotAction action);

    /**
     * Unregister a custom bot action.
     *
     * @param name action name
     * @return true if success, or false
     */
    public boolean unregisterCustomBotAction(String name);

    public BotCreator botCreator(@NotNull String realName, @NotNull Location location);
}
