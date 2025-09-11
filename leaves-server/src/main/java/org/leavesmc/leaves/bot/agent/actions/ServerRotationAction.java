package org.leavesmc.leaves.bot.agent.actions;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftRotationAction;
import org.leavesmc.leaves.command.CommandContext;

import java.text.DecimalFormat;

public class ServerRotationAction extends AbstractBotAction<ServerRotationAction> {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    public ServerRotationAction() {
        super("rotation", ServerRotationAction::new);
        this.addArgument("yaw", FloatArgumentType.floatArg(-180, 180))
            .suggests((context, builder) -> builder.suggest(
                DF.format(context.getSource().getEntityOrException().getYRot()),
                Component.literal("current player yaw")
            ))
            .setOptional(true);
        this.addArgument("pitch", FloatArgumentType.floatArg(-90, 90))
            .suggests((context, builder) -> builder.suggest(
                DF.format(context.getSource().getEntityOrException().getXRot()),
                Component.literal("current player pitch")
            ))
            .setOptional(true);
    }

    private float yaw = 0.0f;
    private float pitch = 0.0f;

    @Override
    public void loadCommand(@NotNull CommandContext context) throws CommandSyntaxException {
        this.yaw = context.getFloatOrDefault("yaw", context.getSource().getEntityOrException().getYRot());
        this.pitch = context.getFloatOrDefault("pitch", context.getSource().getEntityOrException().getXRot());
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    @Override
    public String getActionDataString(@NotNull ActionData data) {
        data.add("yaw", DF.format(this.yaw));
        data.add("pitch", DF.format(this.pitch));
        return super.getActionDataString(data);
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
        this.setYaw(nbt.getFloat("yaw").orElseThrow());
        this.setPitch(nbt.getFloat("pitch").orElseThrow());
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        bot.setRot(yaw, pitch);
        return true;
    }

    @Override
    public Object asCraft() {
        return new CraftRotationAction(this);
    }
}
