package org.leavesmc.leaves.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BreakBedrockList {

    private static final Map<Level, Map<BlockPos, Player>> pistonCache = new HashMap<>();
    private static final List<Objective> BBL = new ArrayList<>();
    private static final List<Objective> MBB = new ArrayList<>();
    private static final List<Objective> LBL = new ArrayList<>();

    public static void endTick() {
        for (var map : pistonCache.values()) {
            if (!map.isEmpty()) {
                map.clear();
            }
        }
    }

    public static void onPlayerPlacePiston(Level level, Player player, BlockPos pos) {
        if (LeavesConfig.modify.bedrockBreakList) {
            Direction pistonFacing = level.getBlockState(pos).getValue(DirectionalBlock.FACING);
            BlockPos bedrockPos = pos.relative(pistonFacing);
            if (level.getBlockState(bedrockPos).getBlock() == Blocks.BEDROCK) {
                pistonCache.computeIfAbsent(level, k -> new HashMap<>()).put(bedrockPos, player);
            }
        }
    }

    public static void onPistonBreakBedrock(Level level, BlockPos bedrock) {
        if (LeavesConfig.modify.bedrockBreakList) {
            Map<BlockPos, Player> map = pistonCache.get(level);

            boolean flag = map != null && map.get(bedrock) != null;

            if (flag) {
                if (!BBL.isEmpty()) {
                    Player player = map.get(bedrock);
                    for (Objective objective : BBL) {
                        level.getScoreboard().getOrCreatePlayerScore(player, objective).increment();
                    }
                }
            } else {
                if (!MBB.isEmpty()) {
                    ScoreHolder world = ScoreHolder.forNameOnly("$" + level.dimension().identifier());
                    for (Objective objective : MBB) {
                        level.getScoreboard().getOrCreatePlayerScore(world, objective).increment();
                        level.getScoreboard().getOrCreatePlayerScore(ScoreHolder.forNameOnly("$total"), objective).increment();
                    }
                }
            }

            if (!LBL.isEmpty() && !level.players().isEmpty()) {
                Player closestPlayer = level.getNearestPlayer(bedrock.getX(), bedrock.getY(), bedrock.getZ(), 10.5, null);
                if (closestPlayer != null) {
                    for (Objective objective : LBL) {
                        level.getScoreboard().getOrCreatePlayerScore(closestPlayer, objective).increment();
                    }
                }
            }
        }
    }

    public static void onScoreboardAdd(@NotNull Objective objective) {
        if (LeavesConfig.modify.bedrockBreakList) {
            if (objective.getCriteria() == ObjectiveCriteria.DUMMY) {
                String name = objective.getName();

                int i = name.length() - 4;
                if (i >= 0) {
                    String suffix = name.substring(i);
                    switch (suffix) {
                        case ".bbl" -> BBL.add(objective);
                        case ".mbb" -> MBB.add(objective);
                        case ".lbl" -> LBL.add(objective);
                    }
                }
            }
        }
    }

    public static void onScoreboardRemove(@NotNull Objective objective) {
        if (LeavesConfig.modify.bedrockBreakList) {
            if (objective.getCriteria() == ObjectiveCriteria.DUMMY) {
                String name = objective.getName();

                int i = name.length() - 4;
                if (i >= 0) {
                    String suffix = name.substring(i);
                    switch (suffix) {
                        case ".bbl" -> BBL.remove(objective);
                        case ".mbb" -> MBB.remove(objective);
                        case ".lbl" -> LBL.remove(objective);
                    }
                }
            }
        }
    }
}
