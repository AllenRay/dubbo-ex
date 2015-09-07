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
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ExecutorUtil;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.transport.AbstractServer;
import com.alibaba.dubbo.remoting.transport.dispatcher.ChannelHandlers;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.SystemPropertyUtil;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * com.alibaba.dubbo.remoting.transport.netty4.NettyServer
 * 
 * @author qian.lei
 * @author chao.liuc
 */
public class NettyServer extends AbstractServer implements Server {
    
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private Map<String, Channel>  channels; // <ip:port, channel>

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    //private EventExecutorGroup handleGroup;

    private io.netty.channel.Channel channel;

    public NettyServer(URL url, ChannelHandler handler) throws RemotingException {
        super(url, ChannelHandlers.wrap(handler, ExecutorUtil.setThreadName(url, SERVER_THREAD_POOL_NAME)));
    }


	@Override
    protected void doOpen() throws Throwable {
        NettyHelper.setNettyLoggerFactory();
        
        int threads = getUrl().getPositiveParameter(Constants.IO_THREADS_KEY, Constants.DEFAULT_IO_THREADS);//Runtime.getRuntime().availableProcessors()*2;
        
        String name = SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim();
        bootstrap = new ServerBootstrap();
        if (!name.startsWith("linux")) {
            bossGroup = new NioEventLoopGroup(threads,  new NettyThreadFactory(null, "NettyServerBoss", true));
            workerGroup = new NioEventLoopGroup(threads, new NettyThreadFactory(null, "NettyServerWorker", true));
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
        }else{
            bossGroup = new EpollEventLoopGroup(threads,  new NettyThreadFactory(null, "NettyServerBoss", true));
            workerGroup = new EpollEventLoopGroup(threads, new NettyThreadFactory(null, "NettyServerWorker", true));
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(EpollServerSocketChannel.class);
        }
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, getTimeout());
		bootstrap.childOption(ChannelOption.SO_RCVBUF, 100*1024);
		bootstrap.childOption(ChannelOption.SO_SNDBUF, 100*1024);
		bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);
		
		//选择内存分配模型
		bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		
        
        final NettyHandler nettyHandler = new NettyHandler(getUrl(), this);
        channels = nettyHandler.getChannels();
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
        	@Override
            public void initChannel(SocketChannel channel) {
                NettyCodecAdapter adapter = new NettyCodecAdapter(getCodec() ,getUrl(), NettyServer.this);
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("decoder", adapter.getDecoder());
                pipeline.addLast("encoder", adapter.getEncoder());
                pipeline.addLast("handler", nettyHandler);
            }
        });
        
        //this.setExecutorService(handleGroup.next());
        // bind
        ChannelFuture future = bootstrap.bind(getBindAddress());
        future.awaitUninterruptibly();
        channel = future.channel();
    }

    @Override
    protected void doClose() throws Throwable {
    	//super.close();
        try {
            if (channel != null) {
                // unbind.
            	channel.closeFuture();
                channel.close();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            Collection<Channel> channels = getChannels();
            if (channels != null && channels.size() > 0) {
                for (com.alibaba.dubbo.remoting.Channel channel : channels) {
                    try {
                        channel.close();
                    } catch (Throwable e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
        	if(bossGroup != null){
        		bossGroup.shutdownGracefully();
        		bossGroup = null;
        	}
        	if(workerGroup != null){
        		workerGroup.shutdownGracefully();
        		workerGroup = null;
        	}
            if (bootstrap != null) { 
                // release external resource.
                //bootstrap.releaseExternalResources();
            	bootstrap = null;
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (channels != null) {
                channels.clear();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }
    
    public Collection<Channel> getChannels() {
        Collection<Channel> chs = new HashSet<Channel>();
        for (Channel channel : this.channels.values()) {
            if (channel.isConnected()) {
                chs.add(channel);
            } else {
                channels.remove(NetUtils.toAddressString(channel.getRemoteAddress()));
            }
        }
        return chs;
    }

    public Channel getChannel(InetSocketAddress remoteAddress) {
        return channels.get(NetUtils.toAddressString(remoteAddress));
    }

    public boolean isBound() {
        return channel.isRegistered();
    }

}