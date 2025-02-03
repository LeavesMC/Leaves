package org.leavesmc.leaves.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalForcer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// Powered by NetherPortalFix(https://github.com/TwelveIterationMods/NetherPortalFix)
public class ReturnPortalManager {

    private static final int MAX_PORTAL_DISTANCE_SQ = 16;
    private static final String RETURN_PORTAL_LIST = "ReturnPortalList";
    private static final String RETURN_PORTAL_UID = "UID";
    private static final String FROM_DIM = "FromDim";
    private static final String FROM_POS = "FromPos";
    private static final String TO_POS = "ToPos";

    public static BlockPos findPortalAt(Player player, ResourceKey<Level> dim, BlockPos pos) {
        MinecraftServer server = player.level().getServer();
        if (server != null) {
            ServerLevel fromWorld = server.getLevel(dim);
            if (fromWorld != null) {
                PortalForcer portalForcer = fromWorld.getPortalForcer();
                return portalForcer.findClosestPortalPosition(pos, false, fromWorld.getWorldBorder()).orElse(null);
            }
        }

        return null;
    }

    public static ListTag getPlayerPortalList(Player player) {
        CompoundTag data = player.getLeavesData();
        ListTag list = data.getList(RETURN_PORTAL_LIST, Tag.TAG_COMPOUND);
        data.put(RETURN_PORTAL_LIST, list);
        return list;
    }

    @Nullable
    public static ReturnPortal findReturnPortal(ServerPlayer player, ResourceKey<Level> fromDim, BlockPos fromPos) {
        ListTag portalList = getPlayerPortalList(player);
        for (Tag entry : portalList) {
            CompoundTag portal = (CompoundTag) entry;
            ResourceKey<Level> entryFromDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(portal.getString(FROM_DIM)));
            if (entryFromDim == fromDim) {
                BlockPos portalTrigger = BlockPos.of(portal.getLong(FROM_POS));
                if (portalTrigger.distSqr(fromPos) <= MAX_PORTAL_DISTANCE_SQ) {
                    final var uid = portal.hasUUID(RETURN_PORTAL_UID) ? portal.getUUID(RETURN_PORTAL_UID) : UUID.randomUUID();
                    final var pos = BlockPos.of(portal.getLong(TO_POS));
                    return new ReturnPortal(uid, pos);
                }
            }
        }

        return null;
    }

    public static void storeReturnPortal(ServerPlayer player, ResourceKey<Level> fromDim, BlockPos fromPos, BlockPos toPos) {
        ListTag portalList = getPlayerPortalList(player);
        ReturnPortal returnPortal = findReturnPortal(player, fromDim, fromPos);
        if (returnPortal != null) {
            removeReturnPortal(player, returnPortal);
        }

        CompoundTag portalCompound = new CompoundTag();
        portalCompound.putUUID(RETURN_PORTAL_UID, UUID.randomUUID());
        portalCompound.putString(FROM_DIM, String.valueOf(fromDim.location()));
        portalCompound.putLong(FROM_POS, fromPos.asLong());
        portalCompound.putLong(TO_POS, toPos.asLong());
        portalList.add(portalCompound);
    }

    public static void removeReturnPortal(ServerPlayer player, ReturnPortal portal) {
        // This doesn't check if it's the right toDim, but it's probably so rare for positions to actually overlap that I don't care
        ListTag portalList = getPlayerPortalList(player);
        for (int i = 0; i < portalList.size(); i++) {
            CompoundTag entry = (CompoundTag) portalList.get(i);
            if (entry.hasUUID(RETURN_PORTAL_UID) && entry.getUUID(RETURN_PORTAL_UID).equals(portal.uid)) {
                portalList.remove(i);
                break;
            }
        }
    }

    public record ReturnPortal(UUID uid, BlockPos pos) {
    }
}
