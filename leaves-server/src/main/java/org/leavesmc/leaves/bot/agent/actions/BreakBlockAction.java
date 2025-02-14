package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;

public class BreakBlockAction extends AbstractTimerAction<BreakBlockAction> {

    public BreakBlockAction() {
        super("break", BreakBlockAction::new);
    }

    private BlockPos lastPos = null;
    private int destroyProgressTime = 0;
    private int lastSentState = -1;

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        Block block = bot.getBukkitEntity().getTargetBlockExact(5);
        if (block != null) {
            BlockPos pos = ((CraftBlock) block).getPosition();

            if (lastPos == null || !lastPos.equals(pos)) {
                lastPos = pos;
                destroyProgressTime = 0;
                lastSentState = -1;
            }

            BlockState iblockdata = bot.level().getBlockState(pos);
            if (!iblockdata.isAir()) {
                bot.swing(InteractionHand.MAIN_HAND);

                if (iblockdata.getDestroyProgress(bot, bot.level(), pos) >= 1.0F) {
                    bot.gameMode.destroyAndAck(pos, 0, "insta mine");
                    bot.level().destroyBlockProgress(bot.getId(), pos, -1);
                    bot.updateItemInHand(InteractionHand.MAIN_HAND);
                    finalBreak();
                    return true;
                }

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
        return false;
    }

    private void finalBreak() {
        lastPos = null;
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
}
