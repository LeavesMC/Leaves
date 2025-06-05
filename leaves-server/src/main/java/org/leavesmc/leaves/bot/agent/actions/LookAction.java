package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.AbstractBotAction;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.text.DecimalFormat;
import java.util.List;

public class LookAction extends AbstractBotAction<LookAction> {

    private static final DecimalFormat DF = new DecimalFormat("0.0");

    public LookAction() {
        super("look", CommandArgument.of(CommandArgumentType.DOUBLE, CommandArgumentType.DOUBLE, CommandArgumentType.DOUBLE), LookAction::new);
        this.setSuggestion(0, (sender, arg) -> sender instanceof ServerPlayer player ? Pair.of(List.of(DF.format(player.getX())), "<X>") : Pair.of(List.of("0"), "<X>"));
        this.setSuggestion(1, (sender, arg) -> sender instanceof ServerPlayer player ? Pair.of(List.of(DF.format(player.getY())), "<Y>") : Pair.of(List.of("0"), "<Y>"));
        this.setSuggestion(2, (sender, arg) -> sender instanceof ServerPlayer player ? Pair.of(List.of(DF.format(player.getZ())), "<Z>") : Pair.of(List.of("0"), "<Z>"));
    }

    private Vector pos;

    @Override
    public void loadCommand(@Nullable ServerPlayer player, @NotNull CommandArgumentResult result) throws IllegalArgumentException {
        Vector pos = result.readVector();
        if (pos != null) {
            this.setPos(pos).setInitialTickDelay(0).setInitialTickInterval(1).setInitialNumber(1);
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
        this.setPos(
            new Vector(
                nbt.getDouble("x").orElse(0.0),
                nbt.getDouble("y").orElse(0.0),
                nbt.getDouble("z").orElse(0.0)
            )
        );
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
