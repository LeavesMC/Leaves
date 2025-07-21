package org.leavesmc.leaves.bot.agent.configs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.waypoints.ServerWaypointManager;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.agent.AbstractBotConfig;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.util.List;

public class LocatorBarConfig extends AbstractBotConfig<Boolean> {

    public static final String NAME = "enable_locator_bar";

    private boolean value;

    public LocatorBarConfig() {
        super(NAME, CommandArgument.of(CommandArgumentType.BOOLEAN).setSuggestion(0, List.of("true", "false")));
        this.value = LeavesConfig.modify.fakeplayer.inGame.enableLocatorBar;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Boolean value) throws IllegalArgumentException {
        this.value = value;
        ServerWaypointManager manager = this.bot.level().getWaypointManager();
        if (value) {
            manager.trackWaypoint(this.bot);
        } else {
            manager.untrackWaypoint(this.bot);
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putBoolean(NAME, this.getValue());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        this.setValue(nbt.getBooleanOr(NAME, LeavesConfig.modify.fakeplayer.inGame.enableLocatorBar));
    }
}