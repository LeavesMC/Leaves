package org.leavesmc.leaves.bot;

import com.google.gson.JsonElement;
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

    // Leaves - Paper 26.1: parseLocal was replaced by parse(DataFixer, JsonElement);
    // override to preserve the "bot stats are never persisted" semantics (defensive — the
    // UNKOWN_FILE marker path shouldn't exist, but if it did the parent would try to parse it)
    @Override
    public void parse(@NotNull DataFixer fixerUpper, @NotNull JsonElement element) {
    }

    @Override
    public int getValue(@NotNull Stat<?> stat) {
        return 0;
    }
}
