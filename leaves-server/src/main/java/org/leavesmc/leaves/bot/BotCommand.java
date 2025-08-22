package org.leavesmc.leaves.bot;

import net.minecraft.Util;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.subcommands.BotConfigCommand;
import org.leavesmc.leaves.bot.subcommands.BotCreateCommand;
import org.leavesmc.leaves.bot.subcommands.BotListCommand;
import org.leavesmc.leaves.bot.subcommands.BotLoadCommand;
import org.leavesmc.leaves.bot.subcommands.BotRemoveCommand;
import org.leavesmc.leaves.bot.subcommands.BotSaveCommand;
import org.leavesmc.leaves.command.LeavesRootCommand;
import org.leavesmc.leaves.command.LeavesSubcommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BotCommand extends LeavesRootCommand {

    // subcommand label -> subcommand
    private static final Map<String, LeavesSubcommand> SUBCOMMANDS = Util.make(() -> {
        final Map<Set<String>, LeavesSubcommand> commands = new HashMap<>();
        commands.put(Set.of("create"), new BotCreateCommand());
        commands.put(Set.of("remove"), new BotRemoveCommand());
//        commands.put(Set.of("action"), new BotActionCommand());
        commands.put(Set.of("config"), new BotConfigCommand());
        commands.put(Set.of("save"), new BotSaveCommand());
        commands.put(Set.of("load"), new BotLoadCommand());
        commands.put(Set.of("list"), new BotListCommand());

        return commands.entrySet().stream()
            .flatMap(entry -> entry.getKey().stream().map(s -> Map.entry(s, entry.getValue())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    });

    public BotCommand() {
        super("bot", "FakePlayer Command", "bukkit.command.bot", SUBCOMMANDS);
    }

    @Override
    public boolean isEnabled() {
        return LeavesConfig.modify.fakeplayer.enable;
    }
}