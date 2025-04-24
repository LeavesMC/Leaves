package org.leavesmc.leaves.bytebuf;

import com.google.gson.JsonElement;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public interface Bytebuf {

    static Bytebuf buf(int size) {
        return Bukkit.getBytebufManager().newBytebuf(size);
    }

    static Bytebuf buf() {
        return buf(128);
    }

    static Bytebuf of(byte[] bytes) {
        return Bukkit.getBytebufManager().toBytebuf(bytes);
    }

    byte[] toArray();

    Bytebuf skipBytes(int i);

    int readerIndex();

    Bytebuf readerIndex(int i);

    int writerIndex();

    Bytebuf writerIndex(int i);

    Bytebuf resetReaderIndex();

    Bytebuf resetWriterIndex();

    Bytebuf writeByte(int i);

    byte readByte();

    Bytebuf writeBoolean(boolean b);

    boolean readBoolean();

    Bytebuf writeFloat(float f);

    float readFloat();

    Bytebuf writeDouble(double d);

    double readDouble();

    Bytebuf writeShort(int i);

    short readShort();

    Bytebuf writeInt(int i);

    int readInt();

    Bytebuf writeLong(long i);

    long readLong();

    Bytebuf writeVarInt(int i);

    int readVarInt();

    Bytebuf writeVarLong(long i);

    long readVarLong();

    Bytebuf writeUUID(UUID uuid);

    UUID readUUID();

    Bytebuf writeEnum(Enum<?> instance);

    <T extends Enum<T>> T readEnum(Class<T> enumClass);

    Bytebuf writeUTFString(String utf);

    String readUTFString();

    Bytebuf writeComponentPlain(String str);

    String readComponentPlain();

    Bytebuf writeComponentJson(JsonElement json);

    JsonElement readComponentJson();

    Bytebuf writeItemStack(ItemStack itemStack);

    ItemStack readItemStack();

    Bytebuf writeItemStackList(List<ItemStack> itemStacks);

    List<ItemStack> readItemStackList();

    Bytebuf copy();

    void retain();

    boolean release();
}
