package org.leavesmc.leaves.bot;

import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;

import java.util.Optional;

public interface IPlayerDataStorage {

    void save(Player player);

    Optional<ValueInput> load(Player player, ProblemReporter reporter);
}
