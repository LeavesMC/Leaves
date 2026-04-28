package org.leavesmc.leaves.bytebuf;

public interface BytebufAllocator {

    Bytebuf newBytebuf(int size);

    Bytebuf toBytebuf(byte[] bytes);
}
