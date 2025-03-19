package org.leavesmc.leaves.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.boss.wither.WitherBoss;

public enum ForcePeacefulModeSwitchType {
    BLAZE(Blaze.class),
    CREEPER(Creeper.class),
    DROWNED(Drowned.class),
    ELDER_GUARDIAN(ElderGuardian.class),
    ENDERMAN(EnderMan.class),
    ENDERMITE(Endermite.class),
    EVOKER(Evoker.class),
    GHAST(Ghast.class),
    GIANT(Giant.class),
    GUARDIAN(Guardian.class),
    HOGLIN(Hoglin.class),
    HUSK(Husk.class),
    ILLUSIONER(Illusioner.class),
    MAGMA_CUBE(MagmaCube.class),
    PHANTOM(Phantom.class),
    PIGLIN(Piglin.class),
    PIGLIN_BRUTE(PiglinBrute.class),
    PILLAGER(Pillager.class),
    RAVAGER(Ravager.class),
    SHULKER(Shulker.class),
    SILVERFISH(Silverfish.class),
    SKELETON(Skeleton.class),
    SLIME(Slime.class),
    SPIDER(Spider.class),
    STRAY(Stray.class),
    VEX(Vex.class),
    VINDICATOR(Vindicator.class),
    WARDEN(Warden.class),
    WITCH(Witch.class),
    WITHER_SKELETON(WitherSkeleton.class),
    ZOGLIN(Zoglin.class),
    ZOMBIE(Zombie.class),
    ZOMBIE_VILLAGER(ZombieVillager.class),
    ZOMBIFIED_PIGLIN(ZombifiedPiglin.class),
    WITHER(WitherBoss.class),
    CAVE_SPIDER(CaveSpider.class),
    BREEZE(Breeze.class),
    BOGGED(Bogged.class);

    private final Class<? extends Entity> entityClass;

    ForcePeacefulModeSwitchType(Class<? extends Entity> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<? extends Entity> getEntityClass() {
        return entityClass;
    }
}
