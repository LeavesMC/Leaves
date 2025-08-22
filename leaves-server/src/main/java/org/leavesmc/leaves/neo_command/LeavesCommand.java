package org.leavesmc.leaves.neo_command;

import org.leavesmc.leaves.neo_command.subcommands.ConfigCommand;

public class LeavesCommand extends LiteralNode {
    public LeavesCommand() {
        super("leaves_new");
        children(ConfigCommand::new);
    }
}
