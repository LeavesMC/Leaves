package org.leavesmc.leaves.neo_command.bot;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.Bot;
import org.leavesmc.leaves.entity.bot.CraftBot;
import org.leavesmc.leaves.neo_command.CommandContext;
import org.leavesmc.leaves.neo_command.CustomArgumentType;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class BotArgument implements CustomArgumentType<ServerBot, String> {
    @Override
    public ArgumentType<String> getBaseArgumentType() {
        return StringArgumentType.word();
    }

    @Override
    public ServerBot transform(String value) throws CommandSyntaxException {
        CraftBot craftBot = (CraftBot) Bukkit.getBotManager().getBot(value);
        if (craftBot == null) {
            throw new CommandSyntaxException(
                CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(),
                Component.literal("Bot with name '" + value + "' does not exist")
            );
        }
        return craftBot.getHandle();
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext context, SuggestionsBuilder builder) throws CommandSyntaxException {
        Collection<Bot> bots = Bukkit.getBotManager().getBots();
        if (bots.isEmpty()) {
            return builder
                .suggest("<NO BOT EXISTS>", net.minecraft.network.chat.Component.literal("There are no bots in the server, create one first."))
                .buildFuture();
        }
        bots.stream().map(Player::getName).forEach(builder::suggest);
        return builder.buildFuture();
    }
}
