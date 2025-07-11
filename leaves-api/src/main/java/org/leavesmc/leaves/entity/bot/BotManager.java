package org.leavesmc.leaves.entity.bot;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.entity.bot.action.BotAction;

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
    @Nullable Bot getBot(@NotNull UUID uuid);

    /**
     * Gets a fakeplayer object by the given name.
     *
     * @param name the name to look up
     * @return a fakeplayer if one was found, null otherwise
     */
    @Nullable Bot getBot(@NotNull String name);

    /**
     * Gets a view of all currently logged in fakeplayers. This view is a reused object, making some operations like Collection.size() zero-allocation.
     *
     * @return a view of fakeplayers.
     */
    Collection<Bot> getBots();

    /**
     * Create a bot action by class.
     *
     * @param type action class
     * @return a bot action instance if one was found, null otherwise
     */
    <T extends BotAction<T>> T newAction(@NotNull Class<T> type);

    BotCreator botCreator(@NotNull String realName, @NotNull Location location);
}
