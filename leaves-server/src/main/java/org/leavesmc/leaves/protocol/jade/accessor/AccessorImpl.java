package org.leavesmc.leaves.protocol.jade.accessor;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public abstract class AccessorImpl<T extends HitResult> implements Accessor<T> {

    private final Level level;
    private final Player player;
    private final CompoundTag serverData;
    private final Supplier<T> hit;
    private final boolean serverConnected;
    private final boolean showDetails;
    protected boolean verify;
    private DynamicOps<Tag> ops;
    private RegistryFriendlyByteBuf buffer;

    public AccessorImpl(Level level, Player player, CompoundTag serverData, Supplier<T> hit, boolean serverConnected, boolean showDetails) {
        this.level = level;
        this.player = player;
        this.hit = hit;
        this.serverConnected = serverConnected;
        this.showDetails = showDetails;
        this.serverData = serverData == null ? new CompoundTag() : serverData.copy();
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public final @NotNull CompoundTag getServerData() {
        return serverData;
    }

    @Override
    public DynamicOps<Tag> nbtOps() {
        if (ops == null) {
            ops = RegistryOps.create(NbtOps.INSTANCE, level.registryAccess());
        }
        return ops;
    }

    @Override
    public <D> Optional<D> readData(MapDecoder<D> codec) {
        MapLike<Tag> mapLike = nbtOps().getMap(serverData).getOrThrow();
        return codec.decode(nbtOps(), mapLike).result();
    }

    @Override
    public <D> void writeData(MapEncoder<D> codec, D value) {
        Tag tag = codec.encode(value, nbtOps(), nbtOps().mapBuilder()).build(new CompoundTag()).getOrThrow();
        serverData.merge((CompoundTag) tag);
    }

    private RegistryFriendlyByteBuf buffer() {
        if (buffer == null) {
            buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
        }
        buffer.clear();
        return buffer;
    }

    @Override
    public <D> Optional<D> decodeFromNbt(StreamDecoder<RegistryFriendlyByteBuf, D> codec, Tag tag) {
        try {
            RegistryFriendlyByteBuf buffer = buffer();
            buffer.writeBytes(((ByteArrayTag) tag).getAsByteArray());
            D decoded = codec.decode(buffer);
            return Optional.of(decoded);
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <D> Tag encodeAsNbt(StreamEncoder<RegistryFriendlyByteBuf, D> streamCodec, D value) {
        RegistryFriendlyByteBuf buffer = buffer();
        streamCodec.encode(buffer, value);
        ByteArrayTag tag = new ByteArrayTag(ArrayUtils.subarray(buffer.array(), 0, buffer.readableBytes()));
        buffer.clear();
        return tag;
    }

    @Override
    public T getHitResult() {
        return hit.get();
    }

    /**
     * Returns true if dedicated server has Jade installed.
     */
    @Override
    public boolean isServerConnected() {
        return serverConnected;
    }

    @Override
    public boolean showDetails() {
        return showDetails;
    }

    @Override
    public abstract ItemStack getPickedResult();

    public void requireVerification() {
        verify = true;
    }

    @Override
    public float tickRate() {
        return getLevel().tickRateManager().tickrate();
    }
}
