package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.BotAction;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;

public class RotateAction extends BotAction<RotateAction> {

    public RotateAction() {
        super("rotate", CommandArgument.EMPTY, RotateAction::new);
    }

    private ServerPlayer player;

    @Override
    public void loadCommand(@Nullable ServerPlayer player, @NotNull CommandArgumentResult result) {
        this.setPlayer(player).setTickDelay(0).setNumber(1);
    }

    public RotateAction setPlayer(ServerPlayer player) {
        this.player = player;
        return this;
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putString("actionName", "look"); // to player loc
        nbt.putDouble("x", player.getX());
        nbt.putDouble("y", player.getY());
        nbt.putDouble("z", player.getZ());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        bot.faceLocation(player.getBukkitEntity().getLocation());
        return true;
    }
}
