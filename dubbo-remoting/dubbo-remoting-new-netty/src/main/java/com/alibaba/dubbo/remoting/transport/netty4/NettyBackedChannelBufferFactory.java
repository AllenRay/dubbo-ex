package com.alibaba.dubbo.remoting.transport.netty4;

import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.nio.ByteBuffer;

/**
 * Wrap netty dynamic channel buffer.
 *
 * @author <a href="mailto:gang.lvg@taobao.com">kimi</a>
 */
public class NettyBackedChannelBufferFactory implements ChannelBufferFactory {

    private static final NettyBackedChannelBufferFactory INSTANCE = new NettyBackedChannelBufferFactory();

    private final PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;

    public static ChannelBufferFactory getInstance() {
        return INSTANCE;
    }

    public ChannelBuffer getBuffer(int capacity) {
        return new NettyBackedChannelBuffer(allocator.buffer(capacity));
    }

    public ChannelBuffer getBuffer(byte[] array, int offset, int length) {
        ByteBuf buffer =allocator.buffer(length);
        buffer.writeBytes(array, offset, length);
        return new NettyBackedChannelBuffer(buffer);
    }

    public ChannelBuffer getBuffer(ByteBuffer nioBuffer) {
        return new NettyBackedChannelBuffer(allocator.buffer().writeBytes(nioBuffer));
    }
}
