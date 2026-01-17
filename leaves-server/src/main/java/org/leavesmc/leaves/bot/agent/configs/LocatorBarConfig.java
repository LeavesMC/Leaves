package org.leavesmc.leaves.bot.agent.configs;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.waypoints.ServerWaypointManager;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.command.CommandContext;

public class LocatorBarConfig extends AbstractBotConfig<Boolean> {
    private boolean value;

    public LocatorBarConfig() {
        super("enable_locator_bar", BoolArgumentType.bool());
        this.value = LeavesConfig.modify.fakeplayer.inGame.enableLocatorBar;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(@NotNull Boolean value) throws IllegalArgumentException {
        this.value = value;
        ServerWaypointManager manager = this.bot.level().getWaypointManager();
        if (value) {
            manager.trackWaypoint(this.bot);
        } else {
            manager.untrackWaypoint(this.bot);
        }
    }

    @Override
    public Boolean loadFromCommand(@NotNull CommandContext context) {
        return context.getBoolean(getName());
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putBoolean(getName(), this.getValue());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        this.setValue(nbt.getBooleanOr(getName(), LeavesConfig.modify.fakeplayer.inGame.enableLocatorBar));
    }
}