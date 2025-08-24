package org.leavesmc.leaves.command.bot.subcommands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.ArgumentNode;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.bot.BotSubcommand;

import java.util.List;
import java.util.Objects;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.minecraft.commands.arguments.DimensionArgument.getDimension;


public class ListCommand extends BotSubcommand {

    public ListCommand() {
        super("list");
        children(WorldArgument::new);
    }

    @Override
    protected boolean execute(@NotNull CommandContext context) throws CommandSyntaxException {
        Component msg = Bukkit.getWorlds().stream()
            .map(ListCommand::getBotListMessage)
            .filter(Objects::nonNull)
            .reduce((a, b) -> a.append(text("\n")).append(b))
            .orElseGet(() -> text("No bots on the server", GRAY));
        context.getSender().sendMessage(join(noSeparators(),
            text("Total bot number: ", GRAY),
            text(BotList.INSTANCE.bots.size(), AQUA).hoverEvent(showText(text("current bot count"))),
            text("/", GRAY),
            text(LeavesConfig.modify.fakeplayer.limit, AQUA).hoverEvent(showText(text("bot count limit")))
        ));
        context.getSender().sendMessage(msg);
        return true;
    }

    protected static @Nullable Component getBotListMessage(@NotNull World world) {
        BotList botList = BotList.INSTANCE;
        List<ServerBot> botsInLevel = botList.bots.stream()
            .filter((bot) -> bot.getBukkitEntity().getWorld().equals(world))
            .toList();
        if (botsInLevel.isEmpty()) {
            return null;
        }
        Component botsMsg = botsInLevel.stream()
            .map(Player::getDisplayName)
            .map(PaperAdventure::asAdventure)
            .reduce((a, b) -> a.append(text(", ", GRAY)).append(b))
            .get();
        String worldLocation = ((CraftWorld) world).getHandle().dimension().location().toString();
        return join(noSeparators(),
            text(world.getName(), AQUA).hoverEvent(showText(text(worldLocation))),
            text(" (" + botsInLevel.size() + ")\n", GRAY),
            botsMsg
        );
    }

    private static class WorldArgument extends ArgumentNode<ResourceLocation> {

        protected WorldArgument() {
            super("world", DimensionArgument.dimension());
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) throws CommandSyntaxException {
            ServerLevel dimension = getDimension(context.getMojangContext(), "world");
            Component botListMessage = getBotListMessage(dimension.getWorld());
            CommandSender sender = context.getSender();
            if (botListMessage == null) {
                sender.sendMessage(text("No bots in that world", RED));
            } else {
                sender.sendMessage(text("Bot in ").append(botListMessage));
            }
            return true;
        }
    }
}
