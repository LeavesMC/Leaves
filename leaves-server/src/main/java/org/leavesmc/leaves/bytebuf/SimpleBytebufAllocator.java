package org.leavesmc.leaves.bytebuf;

import io.netty.buffer.Unpooled;

public class SimpleBytebufAllocator implements BytebufAllocator {

    @Override
    public Bytebuf newBytebuf(int size) {
        return new WrappedBytebuf(Unpooled.buffer(size));
    }

    @Override
    public Bytebuf toBytebuf(byte[] bytes) {
        return new WrappedBytebuf(Unpooled.wrappedBuffer(bytes));
    }
}
