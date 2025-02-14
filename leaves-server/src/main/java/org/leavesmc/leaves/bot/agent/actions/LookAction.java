package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.BotAction;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.util.List;

public class LookAction extends BotAction<LookAction> {

    public LookAction() {
        super("look", CommandArgument.of(CommandArgumentType.DOUBLE, CommandArgumentType.DOUBLE, CommandArgumentType.DOUBLE), LookAction::new);
        this.setTabComplete(0, List.of("<X>"));
        this.setTabComplete(1, List.of("<Y>"));
        this.setTabComplete(2, List.of("<Z>"));
    }

    private Vector pos;

    @Override
    public void loadCommand(@Nullable ServerPlayer player, @NotNull CommandArgumentResult result) throws IllegalArgumentException {
        Vector pos = result.readVector();
        if (pos != null) {
            this.setPos(pos).setTickDelay(0).setNumber(1);
        } else {
            throw new IllegalArgumentException("pos?");
        }
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putDouble("x", this.pos.getX());
        nbt.putDouble("y", this.pos.getY());
        nbt.putDouble("z", this.pos.getZ());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.setPos(new Vector(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z")));
    }

    public LookAction setPos(Vector pos) {
        this.pos = pos;
        return this;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        bot.look(pos.subtract(bot.getLocation().toVector()), false);
        return true;
    }
}
