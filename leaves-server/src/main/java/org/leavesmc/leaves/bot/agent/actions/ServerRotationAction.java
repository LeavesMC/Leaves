package org.leavesmc.leaves.bot.agent.actions;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.ExtraData;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.entity.bot.actions.CraftRotationAction;

import java.text.DecimalFormat;

public class ServerRotationAction extends AbstractBotAction<ServerRotationAction> {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    public ServerRotationAction() {
        super("rotation");
        this.addArgument("yaw", FloatArgumentType.floatArg(-180, 180))
            .suggests((context, builder) -> {
                CommandSender sender = context.getSender();
                if (sender instanceof Entity entity) {
                    builder.suggest(
                        DF.format(entity.getYaw()),
                        Component.literal("current player yaw")
                    );
                }
            })
            .setOptional(true);
        this.addArgument("pitch", FloatArgumentType.floatArg(-90, 90))
            .suggests((context, builder) -> {
                CommandSender sender = context.getSender();
                if (sender instanceof Entity entity) {
                    builder.suggest(
                        DF.format(entity.getPitch()),
                        Component.literal("current player pitch")
                    );
                }
            })
            .setOptional(true);
    }

    private float yaw = 0.0f;
    private float pitch = 0.0f;

    @Override
    public void loadCommand(@NotNull CommandContext context) {
        CommandSender sender = context.getSender();
        if (sender instanceof Entity entity) {
            this.yaw = entity.getYaw();
            this.pitch = entity.getPitch();
        }
        this.yaw = context.getFloatOrDefault("yaw", this.yaw);
        this.pitch = context.getFloatOrDefault("pitch", this.pitch);
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
    public String getActionDataString(@NotNull ExtraData data) {
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
