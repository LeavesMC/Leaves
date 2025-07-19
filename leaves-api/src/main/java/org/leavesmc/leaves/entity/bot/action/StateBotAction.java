package org.leavesmc.leaves.entity.bot.action;

/**
 * Represents a type of bot action that places the bot in a specific state while the action is active.
 * When the action is stopped, the associated state is removed from the bot.
 *
 * @param <E> The type of entity that this action operates on.
 */
public interface StateBotAction<E> extends BotAction<E> {
}
