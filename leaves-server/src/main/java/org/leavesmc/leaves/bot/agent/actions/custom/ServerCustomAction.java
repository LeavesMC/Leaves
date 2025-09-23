package org.leavesmc.leaves.bot.agent.actions.custom;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.AbstractBotAction;
import org.leavesmc.leaves.entity.bot.action.custom.CustomActionProvider;
import org.leavesmc.leaves.entity.bot.actions.CraftCustomAction;

import java.util.HashMap;
import java.util.Map;

public class ServerCustomAction extends AbstractBotAction<ServerCustomAction> {

    private final CustomActionProvider customActionProvider;

    private final Map<String, Object> metaMap = new HashMap<>();

    public ServerCustomAction(CustomActionProvider customActionProvider) {
        super(customActionProvider.id(), null); // Don't create here
        this.customActionProvider = customActionProvider;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        return customActionProvider.doTick(bot.getBukkitEntity());
    }

    @Override
    public Object asCraft() {
        return new CraftCustomAction(this);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMeta(String key) {
        return (T) metaMap.get(key);
    }

    public <T> void setMeta(String key, T meta) {
        metaMap.put(key, meta);
    }
}
