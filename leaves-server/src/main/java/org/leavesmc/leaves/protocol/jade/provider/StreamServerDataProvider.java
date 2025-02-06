package org.leavesmc.leaves.protocol.jade.provider;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.protocol.jade.accessor.Accessor;

import java.util.Optional;

public interface StreamServerDataProvider<T extends Accessor<?>, D> extends IServerDataProvider<T> {

    @Override
    default void appendServerData(CompoundTag data, T accessor) {
        D value = streamData(accessor);
        if (value != null) {
            data.put(getUid().toString(), accessor.encodeAsNbt(streamCodec(), value));
        }
    }

    default Optional<D> decodeFromData(T accessor) {
        Tag tag = accessor.getServerData().get(getUid().toString());
        if (tag == null) {
            return Optional.empty();
        }
        return accessor.decodeFromNbt(streamCodec(), tag);
    }

    @Nullable
    D streamData(T accessor);

    StreamCodec<RegistryFriendlyByteBuf, D> streamCodec();
}
