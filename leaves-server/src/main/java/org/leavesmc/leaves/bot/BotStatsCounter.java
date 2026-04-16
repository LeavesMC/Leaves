package org.leavesmc.leaves.bot;

import com.mojang.datafixers.DataFixer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path; // Leaves - Paper 26.1: ServerStatsCounter now takes Path instead of File

public class BotStatsCounter extends ServerStatsCounter {

    private static final Path UNKOWN_FILE = Path.of("BOT_STATS_REMOVE_THIS"); // Leaves - Paper 26.1: File -> Path

    public BotStatsCounter(MinecraftServer server) {
        super(server, UNKOWN_FILE);
    }

    @Override
    public void save() {
    }

    @Override
    public void setValue(@NotNull Player player, @NotNull Stat<?> stat, int value) {
    }

    // Leaves - Paper 26.1: parseLocal removed from ServerStatsCounter; the JSON parsing moved to parse(DataFixer, JsonElement)
    // Keeping method gone: our bot stats are stubbed (never saved/loaded), so the old override had no behavior to preserve

    @Override
    public int getValue(@NotNull Stat<?> stat) {
        return 0;
    }
}
