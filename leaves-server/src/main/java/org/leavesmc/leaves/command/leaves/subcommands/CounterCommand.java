package org.leavesmc.leaves.command.leaves.subcommands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.DyeColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.LiteralNode;
import org.leavesmc.leaves.command.leaves.LeavesSubcommand;
import org.leavesmc.leaves.util.HopperCounter;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.spaces;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class CounterCommand extends LeavesSubcommand {

    public CounterCommand() {
        super("counter");
        children(
            EnableNode::new,
            DisableNode::new,
            ResetAllNode::new
        );
        for (DyeColor color : DyeColor.values()) {
            children(() -> new ColorNode(color));
        }
    }

    @Override
    public boolean requires(@NotNull CommandSourceStack source) {
        return LeavesConfig.modify.hopperCounter.enable && super.requires(source);
    }

    @Override
    protected boolean execute(@NotNull CommandContext context) {
        context.getSender().sendMessage(join(spaces(),
            text("Hopper counter is", GRAY),
            text(HopperCounter.isEnabled() ? "enabled" : "disabled", AQUA)
        ));
        return true;
    }

    private static class EnableNode extends LiteralNode {
        private EnableNode() {
            super("enable");
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) {
            CommandSender sender = context.getSender();
            if (HopperCounter.isEnabled()) {
                sender.sendMessage(join(spaces(),
                    text("Hopper counter is already", GRAY),
                    text("enabled", AQUA)
                ));
                return true;
            }
            HopperCounter.setEnabled(true);
            sender.sendMessage(join(spaces(),
                text("Hopper counter is now", GRAY),
                text("enabled", AQUA)
            ));
            return true;
        }
    }

    private static class DisableNode extends LiteralNode {
        private DisableNode() {
            super("disable");
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) {
            CommandSender sender = context.getSender();
            if (!HopperCounter.isEnabled()) {
                sender.sendMessage(join(spaces(),
                    text("Hopper counter is already", GRAY),
                    text("disabled", AQUA)
                ));
                return true;
            }
            HopperCounter.setEnabled(false);
            sender.sendMessage(join(spaces(),
                text("Hopper counter is now", GRAY),
                text("disabled", AQUA)
            ));
            return true;
        }
    }

    private static class ResetAllNode extends LiteralNode {
        private ResetAllNode() {
            super("reset");
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) {
            HopperCounter.resetAll(MinecraftServer.getServer(), false);
            context.getSender().sendMessage(text("Restarted all counters", GRAY));
            return true;
        }
    }

    private static class ColorNode extends LiteralNode {
        private final DyeColor color;
        private final HopperCounter counter;

        private ColorNode(@NotNull DyeColor color) {
            super(color.getName());
            this.color = color;
            this.counter = HopperCounter.getCounter(color);
            children(
                ResetNode::new,
                RealtimeNode::new
            );
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) {
            displayCounter(context.getSender(), false);
            return true;
        }

        private void displayCounter(CommandSender sender, boolean realTime) {
            for (Component component : counter.format(MinecraftServer.getServer(), realTime)) {
                sender.sendMessage(component);
            }
        }

        private class ResetNode extends LiteralNode {
            private ResetNode() {
                super("reset");
            }

            @Override
            protected boolean execute(@NotNull CommandContext context) {
                counter.reset(MinecraftServer.getServer());
                context.getSender().sendMessage(join(spaces(),
                    text("Restarted counter", GRAY),
                    text(color.getName(), TextColor.color(color.getTextColor()))
                ));
                return true;
            }
        }

        private class RealtimeNode extends LiteralNode {
            private RealtimeNode() {
                super("realtime");
            }

            @Override
            protected boolean execute(@NotNull CommandContext context) {
                displayCounter(context.getSender(), true);
                return true;
            }
        }
    }
}
