package com.alibaba.dubbo.remoting.transport.netty4;

import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferFactory;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

/**
 * Wrap netty dynamic channel buffer.
 *
 * @author <a href="mailto:gang.lvg@taobao.com">kimi</a>
 */
public class NettyBackedChannelBufferFactory implements ChannelBufferFactory {

    private static final NettyBackedChannelBufferFactory INSTANCE = new NettyBackedChannelBufferFactory();

    public static ChannelBufferFactory getInstance() {
        return INSTANCE;
    }

    public ChannelBuffer getBuffer(int capacity) {
        return new NettyBackedChannelBuffer(PooledByteBufAllocator.DEFAULT.buffer(capacity));
    }

    public ChannelBuffer getBuffer(byte[] array, int offset, int length) {
        io.netty.buffer.ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(length);
        buffer.writeBytes(array, offset, length);
        return new NettyBackedChannelBuffer(buffer);
    }

    public ChannelBuffer getBuffer(ByteBuffer nioBuffer) {
        return new NettyBackedChannelBuffer(PooledByteBufAllocator.DEFAULT.buffer().writeBytes(nioBuffer));
    }
}
