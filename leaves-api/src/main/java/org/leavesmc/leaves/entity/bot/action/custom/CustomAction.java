package org.leavesmc.leaves.entity.bot.action.custom;

import org.leavesmc.leaves.entity.bot.action.BotAction;

public interface CustomAction extends BotAction<CustomAction> {

    <T> T getMeta(String key);

    <T> void setMeta(String key, T meta);
}
