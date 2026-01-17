package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemToAction;

public class ServerUseItemToAction extends AbstractUseBotAction<ServerUseItemToAction> {

    public ServerUseItemToAction() {
        super("use_to");
    }

    @Override
    protected boolean interact(@NotNull ServerBot bot) {
        EntityHitResult hitResult = bot.getEntityHitResult();
        return useItemTo(bot, hitResult, InteractionHand.MAIN_HAND).consumesAction();
    }

    public static InteractionResult useItemTo(ServerBot bot, EntityHitResult hitResult, InteractionHand hand) {
        if (hitResult == null) {
            return InteractionResult.FAIL;
        }

        Entity entity = hitResult.getEntity();
        if (!bot.level().getWorldBorder().isWithinBounds(entity.blockPosition())) {
            return InteractionResult.FAIL;
        }

        Vec3 vec3 = hitResult.getLocation().subtract(entity.getX(), entity.getY(), entity.getZ());
        bot.updateItemInHand(hand);
        InteractionResult interactionResult = entity.interactAt(bot, vec3, hand);
        if (!interactionResult.consumesAction()) {
            interactionResult = bot.interactOn(hitResult.getEntity(), hand);
        }

        if (shouldSwing(interactionResult)) {
            bot.swing(hand);
        }

        return interactionResult;
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemToAction(this);
    }
}
