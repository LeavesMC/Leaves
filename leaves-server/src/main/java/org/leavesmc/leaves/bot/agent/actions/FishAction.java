package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;

public class FishAction extends AbstractTimerAction<FishAction> {

    public FishAction() {
        super("fish", FishAction::new);
    }

    private int delay = 0;
    private int nowDelay = 0;

    @Override
    public FishAction setTickDelay(int tickDelay) {
        super.setTickDelay(0);
        this.delay = tickDelay;
        return this;
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putInt("fishDelay", this.delay);
        nbt.putInt("fishNowDelay", this.nowDelay);
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.delay = nbt.getInt("fishDelay");
        this.nowDelay = nbt.getInt("fishNowDelay");
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (this.nowDelay > 0) {
            this.nowDelay--;
            return false;
        }

        ItemStack mainHand = bot.getMainHandItem();
        if (mainHand == ItemStack.EMPTY || mainHand.getItem().getClass() != FishingRodItem.class) {
            return false;
        }

        FishingHook fishingHook = bot.fishing;
        if (fishingHook != null) {
            if (fishingHook.currentState == FishingHook.FishHookState.HOOKED_IN_ENTITY) {
                mainHand.use(bot.level(), bot, InteractionHand.MAIN_HAND);
                this.nowDelay = 20;
                return false;
            }
            if (fishingHook.nibble > 0) {
                mainHand.use(bot.level(), bot, InteractionHand.MAIN_HAND);
                this.nowDelay = this.delay;
                return true;
            }
        } else {
            mainHand.use(bot.level(), bot, InteractionHand.MAIN_HAND);
        }

        return false;
    }
}
