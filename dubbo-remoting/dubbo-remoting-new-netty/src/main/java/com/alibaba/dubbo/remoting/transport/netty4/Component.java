package com.alibaba.dubbo.remoting.transport.netty4;

import io.netty.buffer.ByteBuf;

public class Component {
	ByteBuf buf;
	int offset;
	int endOffset;
	int length;
}
