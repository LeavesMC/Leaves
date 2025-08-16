package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftBreakBlockAction;

public class ServerBreakBlockAction extends ServerTimerBotAction<ServerBreakBlockAction> {

    public ServerBreakBlockAction() {
        super("break", ServerBreakBlockAction::new);
    }

    private ItemStack lastItem = null;
    private BlockPos lastPos = null;
    private int destroyProgressTime = 0;
    private int lastSentState = -1;

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        Block block = bot.getBukkitEntity().getTargetBlockExact(5);
        if (block != null) {
            BlockPos pos = ((CraftBlock) block).getPosition();

            BlockState iblockdata = bot.level().getBlockState(pos);
            if (lastPos == null || !lastPos.equals(pos) || lastItem == null || !lastItem.equals(bot.getMainHandItem())) {
                if (lastPos != null && destroyProgressTime > 0) {
                    bot.level().destroyBlockProgress(bot.getId(), lastPos, -1);
                }
                lastItem = bot.getMainHandItem();
                lastPos = pos;
                destroyProgressTime = 0;
                lastSentState = -1;

                if (!iblockdata.isAir()) {
                    bot.swing(InteractionHand.MAIN_HAND);
                    EnchantmentHelper.onHitBlock(
                        bot.level(), bot.getMainHandItem(), bot, bot, EquipmentSlot.MAINHAND, Vec3.atCenterOf(pos), iblockdata,
                        item -> bot.onEquippedItemBroken(item, EquipmentSlot.MAINHAND)
                    );
                    iblockdata.attack(bot.level(), pos, bot);
                    float f = iblockdata.getDestroyProgress(bot, bot.level(), pos);
                    if (f >= 1.0F) {
                        bot.gameMode.destroyAndAck(pos, 0, "insta mine");
                        bot.updateItemInHand(InteractionHand.MAIN_HAND);
                        finalBreak();
                        return true;
                    } else {
                        destroyProgressTime++;
                        int k = (int) (f * 10.0F);
                        bot.level().destroyBlockProgress(bot.getId(), pos, k);
                        lastSentState = k;
                    }
                }
            } else {
                if (!iblockdata.isAir()) {
                    bot.swing(InteractionHand.MAIN_HAND);
                    float damage = this.incrementDestroyProgress(bot, iblockdata, pos);
                    if (damage >= 1.0F) {
                        bot.gameMode.destroyAndAck(pos, 0, "destroyed");
                        bot.level().destroyBlockProgress(bot.getId(), pos, -1);
                        bot.updateItemInHand(InteractionHand.MAIN_HAND);
                        finalBreak();
                        return true;
                    }
                }
            }
        } else {
            if (lastPos != null) {
                bot.level().destroyBlockProgress(bot.getId(), lastPos, -1);
            }
            finalBreak();
        }
        return false;
    }

    private void finalBreak() {
        lastPos = null;
        lastItem = null;
        destroyProgressTime = 0;
        lastSentState = -1;
    }

    private float incrementDestroyProgress(ServerBot bot, @NotNull BlockState state, BlockPos pos) {
        float f = state.getDestroyProgress(bot, bot.level(), pos) * (float) (++destroyProgressTime);
        int k = (int) (f * 10.0F);

        if (k != lastSentState) {
            bot.level().destroyBlockProgress(bot.getId(), pos, k);
            lastSentState = k;
        }

        return f;
    }

    @Override
    public Object asCraft() {
        return new CraftBreakBlockAction(this);
    }
}
