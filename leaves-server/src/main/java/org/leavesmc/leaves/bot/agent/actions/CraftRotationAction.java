package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.bot.action.RotationAction;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

public class CraftRotationAction extends CraftBotAction<RotationAction> implements RotationAction {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    public CraftRotationAction() {
        super("rotation", CommandArgument.of(CommandArgumentType.FLOAT, CommandArgumentType.FLOAT), CraftRotationAction::new);
        this.setSuggestion(0, (sender, arg) -> sender instanceof ServerPlayer player ? Pair.of(List.of(DF.format(player.getYRot())), "[yaw]") : Pair.of(List.of("0"), "<yaw>"));
        this.setSuggestion(0, (sender, arg) -> sender instanceof ServerPlayer player ? Pair.of(List.of(DF.format(player.getXRot())), "[pitch]") : Pair.of(List.of("0"), "<pitch>"));
    }

    @Override
    public Class<RotationAction> getInterfaceClass() {
        return RotationAction.class;
    }

    private float yaw = 0.0f;
    private float pitch = 0.0f;

    @Override
    public void loadCommand(ServerPlayer player, @NotNull CommandArgumentResult result) {
        try {
            this.yaw = result.readFloat(Objects.requireNonNull(player).getYRot());
            this.pitch = result.readFloat(player.getXRot());
        } catch (Exception e) {
            throw new IllegalArgumentException("No valid rotation specified", e);
        }
    }

    @Override
    public RotationAction setYaw(float yaw) {
        this.yaw = yaw;
        return this;
    }

    @Override
    public RotationAction setPitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    @Override
    public float getYaw() {
        return this.yaw;
    }

    @Override
    public float getPitch() {
        return this.pitch;
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putFloat("yaw", this.yaw);
        nbt.putFloat("pitch", this.pitch);
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.setYaw(nbt.getFloat("yaw").orElseThrow()).setPitch(nbt.getFloat("pitch").orElseThrow());
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        bot.setRot(yaw, pitch);
        return true;
    }
}
