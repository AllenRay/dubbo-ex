package com.alibaba.dubbo.remoting.transport.netty4;/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Codec2;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.IOException;
import java.util.List;

/**
 * com.alibaba.dubbo.remoting.transport.netty4.NettyCodecAdapter.
 * 
 * @author qian.lei
 */
final class NettyCodecAdapter {

    private final ChannelHandler encoder = new InternalEncoder();
    
    private final ChannelHandler decoder = new InternalDecoder();

    private final Codec2 codec;
    
    private final URL url;
    
    private final int            bufferSize;
    
    private final com.alibaba.dubbo.remoting.ChannelHandler handler;

    public NettyCodecAdapter(Codec2 codec, URL url, com.alibaba.dubbo.remoting.ChannelHandler handler) {
        this.codec = codec;
        this.url = url;
        this.handler = handler;
        int b = url.getPositiveParameter(Constants.BUFFER_KEY, Constants.DEFAULT_BUFFER_SIZE);
        this.bufferSize = b >= Constants.MIN_BUFFER_SIZE && b <= Constants.MAX_BUFFER_SIZE ? b : Constants.DEFAULT_BUFFER_SIZE;
    }

    public ChannelHandler getEncoder() {
        return encoder;
    }

    public ChannelHandler getDecoder() {
        return decoder;
    }

    @Sharable
    private class InternalEncoder extends MessageToMessageEncoder<Object> {
    	private Netty4WriteChannelBuffer buffer = new Netty4WriteChannelBuffer();
        @Override
        protected void encode(ChannelHandlerContext ctx, Object message, List<Object> out) throws Exception {

        	buffer.initialize(ctx.alloc());
            Channel ch = ctx.channel();
            NettyChannel channel = NettyChannel.getOrAddChannel(ch, url, handler);
            try {
            	codec.encode(channel, buffer, message);
            	if(buffer.readableBytes() > 0){
            		out.add(buffer.toByteBuf());
            	}
            } finally {
            	buffer.clear();
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
        }
    }

    private class InternalDecoder extends SimpleChannelInboundHandler<ByteBuf> {


    	Netty4ReadChannelBuffer buffer = new Netty4ReadChannelBuffer();
 
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			super.channelInactive(ctx);
			buffer.clear();
		}

		@Override
		public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
			super.handlerRemoved(ctx);
			buffer.clear();
		}

		@Override
        public void channelRead0(ChannelHandlerContext ctx, ByteBuf input) throws Exception {
        	int readable = input.readableBytes();
        	if(readable <= 0) return;
            buffer.add(input);
             
            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
            Object msg;
            int saveReaderIndex;

            try {
                // decode object.
                do {
                    saveReaderIndex = buffer.readerIndex();
                    try {
                        msg = codec.decode(channel, buffer);
                    } catch (IOException e) {
                        //buffer = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                    	buffer.compact();
                        throw e;
                    }
                    if (msg == Codec2.DecodeResult.NEED_MORE_INPUT) {
                        buffer.readerIndex(saveReaderIndex);
                        break;
                    } else {
                        if (saveReaderIndex == buffer.readerIndex()) {
                            //buffer = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                        	buffer.compact();
                            throw new IOException("Decode without read data.");
                        }
                        if (msg != null) {
                        	ctx.fireChannelRead(msg);
                        }
                    }
                } while (buffer.readable());
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                buffer.compact();
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            //ctx.write(e);
        	ctx.fireExceptionCaught(cause);
        }
    }
}