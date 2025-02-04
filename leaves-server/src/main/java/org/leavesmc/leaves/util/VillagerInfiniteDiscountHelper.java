package org.leavesmc.leaves.util;

import net.minecraft.world.entity.ai.gossip.GossipType;

public class VillagerInfiniteDiscountHelper {

    public static void doVillagerInfiniteDiscount(boolean value) {
        if (value) {
            GossipType.MAJOR_POSITIVE.max = 100;
            GossipType.MAJOR_POSITIVE.decayPerTransfer = 100;
            GossipType.MINOR_POSITIVE.max = 200;
        } else {
            GossipType.MAJOR_POSITIVE.max = 20;
            GossipType.MAJOR_POSITIVE.decayPerTransfer = 20;
            GossipType.MINOR_POSITIVE.max = 25;
        }
    }
}
