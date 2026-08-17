// LeavesMC - Vanilla recipe sync protocol
// Copyright (c) 2024 LeavesMC
// This file is licensed under the MIT License.

package org.leavesmc.leaves.protocol;

import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.Bukkit;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.plugin.MinecraftInternalPlugin;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;

import java.util.ArrayList;
import java.util.List;

@LeavesProtocol.Register(namespace = "vanilla-recipe-sync")
public class RecipeSyncProtocol implements LeavesProtocol {

    @Override
    public boolean isActive() {
        return LeavesConfig.protocol.vanillaRecipeSync;
    }

    @ProtocolHandler.PlayerJoin
    public static void onPlayerJoin(ServerPlayer player) {
        sendAllRecipes(player);
    }

    @ProtocolHandler.ReloadDataPack
    public static void onDataPackReload() {
        Bukkit.getGlobalRegionScheduler().run(MinecraftInternalPlugin.INSTANCE, (task) -> {
            for (ServerPlayer player : MinecraftServer.getServer().getPlayerList().getPlayers()) {
                sendAllRecipes(player);
            }
        });
    }

    private static void sendAllRecipes(ServerPlayer player) {
        MinecraftServer server = MinecraftServer.getServer();
        List<ClientboundRecipeBookAddPacket.Entry> entries = new ArrayList<>();

        for (RecipeHolder<?> recipeHolder : server.getRecipeManager().getRecipes()) {
            ResourceKey<Recipe<?>> recipeKey = recipeHolder.id();
            server.getRecipeManager().listDisplaysForRecipe(recipeKey, entry ->
                entries.add(new ClientboundRecipeBookAddPacket.Entry(entry, false, false))
            );
        }

        if (!entries.isEmpty()) {
            player.connection.send(new ClientboundRecipeBookAddPacket(entries, true));
        }
    }
}