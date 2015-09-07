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
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

/**
 * com.alibaba.dubbo.remoting.transport.netty4.NettyClient.
 * 
 * @author qian.lei
 * @author william.liangf
 */
public class NettyClient extends AbstractClient {

	private static final Logger logger = LoggerFactory
			.getLogger(NettyClient.class);


	private EventLoopGroup workerGroup;

	private Bootstrap bootstrap = null;

	private Channel channel;

	public NettyClient(final URL url, final ChannelHandler handler)
			throws RemotingException {
		super(url, wrapChannelHandler(url, handler));
	}

	

	@Override
	protected void doOpen() throws Throwable {
		NettyHelper.setNettyLoggerFactory();
		bootstrap = new Bootstrap().group(getWorkerGroup())
				.channel(NioSocketChannel.class)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getTimeout())
		        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

		final NettyHandler nettyHandler = new NettyHandler(getUrl(), this);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			public void initChannel(SocketChannel channel) {
				NettyCodecAdapter adapter = new NettyCodecAdapter(getCodec(),
						getUrl(), NettyClient.this);
				ChannelPipeline pipeline = channel.pipeline();
				pipeline.addLast("decoder", adapter.getDecoder());
				pipeline.addLast("encoder", adapter.getEncoder());
				pipeline.addLast("handler", nettyHandler);
			}
		});
	}
	
	protected EventLoopGroup getWorkerGroup() {
		workerGroup = new NioEventLoopGroup(Constants.DEFAULT_IO_THREADS,
				new NamedThreadFactory("NettyClientTCPWorker", true));
		return workerGroup;
	}

	protected void doConnect() throws Throwable {
		long start = System.currentTimeMillis();
		ChannelFuture future = bootstrap.connect(getConnectAddress());
		try {
			boolean ret = future.awaitUninterruptibly(getConnectTimeout(),
					TimeUnit.MILLISECONDS);

			if (ret && future.isSuccess()) {
				Channel newChannel = future.channel();
				//newChannel.config().setAutoRead(true);
				try {
					// 关闭旧的连接
					Channel oldChannel = NettyClient.this.channel; // copy
																	// reference
					if (oldChannel != null) {
						try {
							if (logger.isInfoEnabled()) {
								logger.info("Close old netty channel "
										+ oldChannel
										+ " on create new netty channel "
										+ newChannel);
							}
							oldChannel.close().syncUninterruptibly();
						} finally {
							NettyChannel
									.removeChannelIfDisconnected(oldChannel);
						}
					}
				} finally {
					if (!newChannel.isActive()) {
						try {
							if (logger.isInfoEnabled()) {
								logger.info("Close new netty channel "
										+ newChannel
										+ ", because the client closed.");
							}
							newChannel.close().syncUninterruptibly();
						} finally {
							NettyClient.this.channel = null;
							NettyChannel
									.removeChannelIfDisconnected(newChannel);
						}
					} else {
						NettyClient.this.channel = newChannel;
					}
				}
			} else if (future.cause() != null) {
				throw new RemotingException(this, "client(url: " + getUrl()
						+ ") failed to connect to server " + getRemoteAddress()
						+ ", error message is:" + future.cause().getMessage(),
						future.cause());
			} else {
				throw new RemotingException(this, "client(url: " + getUrl()
						+ ") failed to connect to server " + getRemoteAddress()
						+ " client-side timeout " + getConnectTimeout()
						+ "ms (elapsed: "
						+ (System.currentTimeMillis() - start)
						+ "ms) from netty client " + NetUtils.getLocalHost()
						+ " using dubbo version " + Version.getVersion());
			}
		} finally {
			if (!isConnected()) {
				future.cancel(true);
			}
		}
	}

	@Override
	protected void doDisConnect() throws Throwable {
		try {
			NettyChannel.removeChannelIfDisconnected(channel);
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
	}

	@Override
	protected void doClose() throws Throwable {

		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
			workerGroup = null;
		}
	}

	@Override
	protected com.alibaba.dubbo.remoting.Channel getChannel() {
		Channel c = channel;
		if (c == null || !c.isActive())
			return null;
		return NettyChannel.getOrAddChannel(c, getUrl(), this);
	}

}