package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.bot.action.ShootAction;

import java.util.Collections;

public class CraftShootAction extends CraftTimerBotAction<ShootAction> implements ShootAction {

    private static final int DEFAULT_DRAWING_TICK = 20;
    private int drawingTick = DEFAULT_DRAWING_TICK;
    private int tickToRelease = -1;

    public CraftShootAction() {
        super("shoot", CommandArgument.of(CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER), CraftShootAction::new);
        this.setSuggestion(3, Pair.of(Collections.singletonList("20"), "[DrawingTick]"));
    }

    @Override
    public @NotNull Class<ShootAction> getActionRegClass() {
        return ShootAction.class;
    }

    @Override
    public void init() {
        super.init();
        tickToRelease = drawingTick;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (!bot.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BOW)) {
            return false;
        }
        tickToRelease--;
        if (tickToRelease >= 0) {
            bot.gameMode.useItem(bot, bot.level(), bot.getItemInHand(InteractionHand.MAIN_HAND), InteractionHand.MAIN_HAND).consumesAction();
            bot.updateItemInHand(InteractionHand.MAIN_HAND);
            return false;
        } else {
            bot.releaseUsingItem();
            tickToRelease = drawingTick;
            return true;
        }
    }

    @Override
    public void loadCommand(ServerPlayer player, @NotNull CommandArgumentResult result) {
        super.loadCommand(player, result);
        this.drawingTick = result.readInt(DEFAULT_DRAWING_TICK);
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putInt("drawingTick", this.drawingTick);
        nbt.putInt("tickToRelease", this.tickToRelease);
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.drawingTick = nbt.getInt("drawingTick").orElseThrow();
        this.tickToRelease = nbt.getInt("tickToRelease").orElseThrow();
    }

    public int getDrawingTick() {
        return drawingTick;
    }

    public ShootAction setDrawingTick(int drawingTick) {
        this.drawingTick = drawingTick;
        return this;
    }
}