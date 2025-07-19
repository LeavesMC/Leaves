package org.leavesmc.leaves.bytebuf;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.MinecraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class WrappedBytebuf implements Bytebuf {

    private final FriendlyByteBuf buf;
    private final RegistryFriendlyByteBuf registryBuf;

    public WrappedBytebuf(ByteBuf buf) {
        if (buf instanceof RegistryFriendlyByteBuf) {
            this.buf = (FriendlyByteBuf) buf;
            this.registryBuf = (RegistryFriendlyByteBuf) buf;
        } else {
            this.buf = new FriendlyByteBuf(buf);
            this.registryBuf = new RegistryFriendlyByteBuf(this.buf, MinecraftServer.getServer().registryAccess());
        }
    }

    public RegistryFriendlyByteBuf getRegistryBuf() {
        return registryBuf;
    }

    @Override
    public byte[] toArray() {
        int length = buf.readableBytes();
        byte[] data = new byte[length];
        buf.getBytes(buf.readerIndex(), data);
        return data;
    }

    @Override
    public Bytebuf skipBytes(int i) {
        buf.skipBytes(i);
        return this;
    }

    @Override
    public int readerIndex() {
        return buf.readerIndex();
    }

    @Override
    public Bytebuf readerIndex(int i) {
        buf.readerIndex(i);
        return this;
    }

    @Override
    public int writerIndex() {
        return buf.writerIndex();
    }

    @Override
    public Bytebuf writerIndex(int i) {
        buf.writerIndex(i);
        return this;
    }

    @Override
    public Bytebuf resetReaderIndex() {
        buf.resetReaderIndex();
        return this;
    }

    @Override
    public Bytebuf resetWriterIndex() {
        buf.resetWriterIndex();
        return this;
    }

    @Override
    public Bytebuf writeByte(int i) {
        buf.writeByte(i);
        return this;
    }

    @Override
    public byte readByte() {
        return buf.readByte();
    }

    @Override
    public Bytebuf writeBoolean(boolean b) {
        buf.writeBoolean(b);
        return this;
    }

    @Override
    public boolean readBoolean() {
        return buf.readBoolean();
    }

    @Override
    public Bytebuf writeFloat(float f) {
        buf.writeFloat(f);
        return this;
    }

    @Override
    public float readFloat() {
        return buf.readFloat();
    }

    @Override
    public Bytebuf writeDouble(double d) {
        buf.writeDouble(d);
        return this;
    }

    @Override
    public double readDouble() {
        return buf.readDouble();
    }

    @Override
    public Bytebuf writeShort(int i) {
        buf.writeShort(i);
        return this;
    }

    @Override
    public short readShort() {
        return buf.readShort();
    }

    @Override
    public Bytebuf writeInt(int i) {
        buf.writeShort(i);
        return this;
    }

    @Override
    public int readInt() {
        return buf.readInt();
    }

    @Override
    public Bytebuf writeLong(long i) {
        buf.writeLong(i);
        return this;
    }

    @Override
    public long readLong() {
        return buf.readLong();
    }

    @Override
    public Bytebuf writeVarInt(int i) {
        this.buf.writeVarInt(i);
        return this;
    }

    @Override
    public int readVarInt() {
        return this.buf.readVarInt();
    }

    @Override
    public Bytebuf writeVarLong(long i) {
        this.buf.writeVarLong(i);
        return this;
    }

    @Override
    public long readVarLong() {
        return this.buf.readVarLong();
    }

    @Override
    public Bytebuf writeUUID(UUID uuid) {
        this.buf.writeUUID(uuid);
        return this;
    }

    @Override
    public UUID readUUID() {
        return this.buf.readUUID();
    }

    @Override
    public Bytebuf writeEnum(Enum<?> instance) {
        this.buf.writeEnum(instance);
        return this;
    }

    @Override
    public <T extends Enum<T>> T readEnum(Class<T> enumClass) {
        return this.buf.readEnum(enumClass);
    }

    @Override
    public Bytebuf writeUTFString(String utf) {
        buf.writeUtf(utf);
        return this;
    }

    @Override
    public String readUTFString() {
        return buf.readUtf();
    }

    @Override
    public Bytebuf writeComponentPlain(String str) {
        ComponentSerialization.STREAM_CODEC.encode(new RegistryFriendlyByteBuf(this.buf, RegistryAccess.EMPTY), Component.literal(str));
        return this;
    }

    @Override
    public String readComponentPlain() {
        return ComponentSerialization.STREAM_CODEC.decode(new RegistryFriendlyByteBuf(buf, RegistryAccess.EMPTY)).getString();
    }

    @Override
    public Bytebuf writeComponentJson(JsonElement json) {
        Component component = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, json).mapOrElse(Pair::getFirst, v -> null);
        if (component == null) {
            throw new IllegalArgumentException("Null can not be serialize to Minecraft chat component");
        }
        ComponentSerialization.STREAM_CODEC.encode(new RegistryFriendlyByteBuf(buf, RegistryAccess.EMPTY), component);
        return this;
    }

    @Override
    public JsonElement readComponentJson() {
        return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, ComponentSerialization.STREAM_CODEC.decode(new RegistryFriendlyByteBuf(buf, RegistryAccess.EMPTY))).getOrThrow();
    }

    @Override
    public Bytebuf writeItemStack(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.unwrap(itemStack);
        net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode(this.registryBuf, nmsItem);
        return this;
    }

    @Override
    public ItemStack readItemStack() {
        net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.decode(this.registryBuf);
        return nmsItem.asBukkitMirror();
    }

    @Override
    public Bytebuf writeItemStackList(List<ItemStack> itemStacks) {
        List<net.minecraft.world.item.ItemStack> nmsItemList = itemStacks.stream().map(CraftItemStack::unwrap).toList();
        net.minecraft.world.item.ItemStack.OPTIONAL_LIST_STREAM_CODEC.encode(this.registryBuf, nmsItemList);
        return this;
    }

    @Override
    public List<ItemStack> readItemStackList() {
        List<net.minecraft.world.item.ItemStack> nmsItemList = net.minecraft.world.item.ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(this.registryBuf);
        return nmsItemList.stream().map(net.minecraft.world.item.ItemStack::asBukkitMirror).toList();
    }

    @Override
    public Bytebuf copy() {
        return new WrappedBytebuf(this.buf.copy());
    }

    @Override
    public void retain() {
        this.buf.retain();
    }

    @Override
    public boolean release() {
        return this.buf.release();
    }
}
