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

    private static final int CATCH_ENTITY_DELAY = 20;

    private int initialFishInterval = 0;
    private int tickToNextFish = 0;

    @Override
    public FishAction setInitialTickInterval(int initialTickInterval) {
        super.setInitialTickInterval(1);
        this.initialFishInterval = initialTickInterval;
        return this;
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putInt("initialFishInterval", this.initialFishInterval);
        nbt.putInt("tickToNextFish", this.tickToNextFish);
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.initialFishInterval = nbt.getInt("initialFishInterval").orElseThrow();
        this.tickToNextFish = nbt.getInt("tickToNextFish").orElseThrow();
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (this.tickToNextFish > 0) {
            this.tickToNextFish--;
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
                this.tickToNextFish = CATCH_ENTITY_DELAY;
                return false;
            }
            if (fishingHook.nibble > 0) {
                mainHand.use(bot.level(), bot, InteractionHand.MAIN_HAND);
                this.tickToNextFish = this.initialFishInterval - 1;
                return true;
            }
        } else {
            mainHand.use(bot.level(), bot, InteractionHand.MAIN_HAND);
        }

        return false;
    }
}
