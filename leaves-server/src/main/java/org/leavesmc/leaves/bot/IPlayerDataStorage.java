package org.leavesmc.leaves.bot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public interface IPlayerDataStorage {

    void save(Player player);

    Optional<CompoundTag> load(NameAndId nameAndId);
}
