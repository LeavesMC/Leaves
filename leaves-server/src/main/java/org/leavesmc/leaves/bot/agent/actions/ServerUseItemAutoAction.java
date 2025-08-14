package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemAutoAction;

import static org.leavesmc.leaves.bot.agent.actions.ServerUseItemAction.useItem;
import static org.leavesmc.leaves.bot.agent.actions.ServerUseItemOnAction.useItemOn;
import static org.leavesmc.leaves.bot.agent.actions.ServerUseItemToAction.useItemTo;

public class ServerUseItemAutoAction extends ServerUseBotAction<ServerUseItemAutoAction> {

    public ServerUseItemAutoAction() {
        super("use_auto", ServerUseItemAutoAction::new);
    }

    @Override
    protected boolean interact(ServerBot bot) {
        HitResult hitResult = getHitResult(bot);
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemStack = bot.getItemInHand(hand);
            if (!itemStack.isItemEnabled(bot.level().enabledFeatures())) {
                return false;
            }

            if (hitResult != null) {
                switch (hitResult.getType()) {
                    case ENTITY -> {
                        EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                        InteractionResult entityResult = useItemTo(bot, entityHitResult, hand);
                        if (entityResult instanceof InteractionResult.Success) {
                            return true;
                        } else if (entityResult instanceof InteractionResult.Pass && entityHitResult.getEntity() instanceof ArmorStand) {
                            return false;
                        }
                    }
                    case BLOCK -> {
                        InteractionResult blockResult = useItemOn(bot, (BlockHitResult) hitResult, hand);
                        if (blockResult instanceof InteractionResult.Success) {
                            return true;
                        } else if (blockResult instanceof InteractionResult.Fail) {
                            return false;
                        }
                    }
                }
            }
            if (!itemStack.isEmpty() && useItem(bot, hand) instanceof InteractionResult.Success) {
                return true;
            }
        }

        return false;
    }

    private static @Nullable HitResult getHitResult(@NotNull ServerBot bot) {
        Vec3 eyePos = bot.getEyePosition();

        EntityHitResult entityHitResult = bot.getEntityHitResult();
        double entityDistance = entityHitResult != null ? entityHitResult.getLocation().distanceToSqr(eyePos) : Double.MAX_VALUE;

        BlockHitResult blockHitResult = bot.getBlockHitResult();
        double blockDistance = blockHitResult != null ? blockHitResult.getLocation().distanceToSqr(eyePos) : Double.MAX_VALUE;

        if (entityDistance == Double.MAX_VALUE && blockDistance == Double.MAX_VALUE) {
            return null;
        } else if (entityDistance < blockDistance) {
            return entityHitResult;
        } else {
            return blockHitResult;
        }
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemAutoAction(this);
    }
}