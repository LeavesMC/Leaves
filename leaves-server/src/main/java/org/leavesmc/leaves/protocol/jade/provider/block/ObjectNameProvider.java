package org.leavesmc.leaves.protocol.jade.provider.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.protocol.jade.JadeProtocol;
import org.leavesmc.leaves.protocol.jade.accessor.BlockAccessor;
import org.leavesmc.leaves.protocol.jade.provider.StreamServerDataProvider;

public abstract class ObjectNameProvider implements StreamServerDataProvider<BlockAccessor, Component> {

    private static final MapCodec<Component> GIVEN_NAME_CODEC = ComponentSerialization.CODEC.fieldOf("given_name");
    private static final ResourceLocation CORE_OBJECT_NAME = JadeProtocol.id("object_name");

    public static class ForBlock extends ObjectNameProvider implements StreamServerDataProvider<BlockAccessor, Component> {
        public static final ForBlock INSTANCE = new ForBlock();

        @Override
        @Nullable
        public Component streamData(BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof Nameable nameable)) {
                return null;
            }
            if (nameable instanceof ChestBlockEntity && accessor.getBlock() instanceof ChestBlock) {
                MenuProvider menuProvider = accessor.getBlockState().getMenuProvider(accessor.getLevel(), accessor.getPosition());
                if (menuProvider != null) {
                    return menuProvider.getDisplayName();
                }
            } else if (nameable.hasCustomName()) {
                return nameable.getDisplayName();
            }
            return null;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, Component> streamCodec() {
            return ComponentSerialization.STREAM_CODEC;
        }
    }


    @Override
    public ResourceLocation getUid() {
        return CORE_OBJECT_NAME;
    }

    @Override
    public int getDefaultPriority() {
        return -10100;
    }
}