package org.leavesmc.leaves.command;

import net.minecraft.Util;
import org.leavesmc.leaves.command.subcommands.BlockUpdateCommand;
import org.leavesmc.leaves.command.subcommands.ConfigCommand;
import org.leavesmc.leaves.command.subcommands.CounterCommand;
import org.leavesmc.leaves.command.subcommands.ReloadCommand;
import org.leavesmc.leaves.command.subcommands.ReportCommand;
import org.leavesmc.leaves.command.subcommands.UpdateCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class LeavesCommand extends LeavesRootCommand {

    public static final String BASE_PERM = "bukkit.command.leaves.";

    // subcommand label -> subcommand
    private static final Map<String, LeavesSubcommand> SUBCOMMANDS = Util.make(() -> {
        final Map<Set<String>, LeavesSubcommand> commands = new HashMap<>();
        commands.put(Set.of("config"), new ConfigCommand());
        commands.put(Set.of("update"), new UpdateCommand());
        commands.put(Set.of("counter"), new CounterCommand());
        commands.put(Set.of("reload"), new ReloadCommand());
        commands.put(Set.of("report"), new ReportCommand());
        commands.put(Set.of("blockupdate"), new BlockUpdateCommand());

        return commands.entrySet().stream()
            .flatMap(entry -> entry.getKey().stream().map(s -> Map.entry(s, entry.getValue())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    });

    public LeavesCommand() {
        super("leaves", "Leaves related commands", "bukkit.command.leaves", SUBCOMMANDS);
    }
}
