package com.alibaba.dubbo.remoting.transport.netty4;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Netty4ReadChannelBuffer implements
		com.alibaba.dubbo.remoting.buffer.ChannelBuffer {

	private List<Component> components = new ArrayList<Component>();
	private int readerIndex = 0;
	private int markReaderIndex = 0;
	private int total = 0;

	public void add(ByteBuf buf) {
		buf.retain();
		Component b = new Component();
		b.buf = buf;
		b.length = buf.readableBytes();
		if (!components.isEmpty()) {
			Component last = components.get(components.size() - 1);
			b.offset = last.endOffset;
		} else {
			b.offset = 0;
		}
		b.endOffset = b.offset + b.length;
		components.add(b);
		this.total = b.endOffset;
	}

	/**
	 * release buffer
	 */
	public void compact() {
		while (!this.components.isEmpty()) {
			Component b = this.components.get(0);
			if (this.readerIndex >= b.endOffset) {
				this.components.remove(0);
				this.readerIndex -= b.length;
				for (Component b1 : this.components) {
					b1.offset -= b.length;
					b1.endOffset -= b.length;
				}
				// 释放
				b.buf.release();
				this.total -= b.length;
			} else {
				break;
			}
		}
	}

	public int compareTo(com.alibaba.dubbo.remoting.buffer.ChannelBuffer arg0) {
		throw new IllegalStateException();
	}

	public int capacity() {
		throw new IllegalStateException();
	}

	public void clear() {
		while (!components.isEmpty()) {
			Component buf = components.remove(0);
			buf.buf.release();
		}
		readerIndex = 0;
	}

	public com.alibaba.dubbo.remoting.buffer.ChannelBuffer copy() {
		throw new IllegalStateException();
	}

	public com.alibaba.dubbo.remoting.buffer.ChannelBuffer copy(int index,
			int length) {
		throw new IllegalStateException();
	}

	public void discardReadBytes() {
		clear();
	}

	public void ensureWritableBytes(int writableBytes) {
		throw new IllegalStateException();
	}

	public com.alibaba.dubbo.remoting.buffer.ChannelBufferFactory factory() {
		throw new IllegalStateException();
	}

	private int getComponentIndex(int offset) {
		int low = 0;
		for (int high = this.components.size(); low <= high;) {
			int mid = low + high >>> 1;
			Component c = this.components.get(mid);
			if (offset >= c.endOffset) {
				low = mid + 1;
			} else if (offset < c.offset) {
				high = mid - 1;
			} else {
				return mid;
			}
		}
		return -1;
	}

	private Component getComponent(int offset) {
		int low = 0;
		for (int high = this.components.size(); low <= high;) {
			int mid = low + high >>> 1;
			Component c = this.components.get(mid);
			if (offset >= c.endOffset) {
				low = mid + 1;
			} else if (offset < c.offset) {
				high = mid - 1;
			} else {
				assert (c.length != 0);
				return c;
			}
		}
		return null;
	}

	public byte getByte(int index) {
		Component buf = this.getComponent(index);
		return buf.buf.getByte(index - buf.offset);
	}

	public void getBytes(int index, byte[] dst) {
		getBytes(index, dst, 0, dst.length);
	}

	public void getBytes(int index, byte[] dst, int dstIndex, int length) {
		if (index < 0 || index >= total)
			throw new IndexOutOfBoundsException();
		int offset = 0;
		int count = this.components.size();
		int startIndex = this.getComponentIndex(index);
		Component b = this.components.get(startIndex);
		offset = index - b.offset;

		int len = length;
		int pos = dstIndex;
		while (startIndex < count && len > 0) {
			int l = Math.min(len, b.length - offset);
			if (offset > 0) {
				b.buf.markReaderIndex();
				b.buf.skipBytes(offset);
				b.buf.readBytes(dst, pos, l);
				b.buf.resetReaderIndex();
			} else {
				b.buf.readBytes(dst, pos, l);
			}
			len -= l;
			pos += l;
			startIndex++;
			offset = 0;
			if (startIndex < count) {
				b = this.components.get(startIndex);
			}
		}
	}

	public void getBytes(int index, ByteBuffer dst) {
		throw new IndexOutOfBoundsException();
	}

	public void getBytes(int index,
			com.alibaba.dubbo.remoting.buffer.ChannelBuffer dst) {
		throw new IndexOutOfBoundsException();

	}

	public void getBytes(int index,
			com.alibaba.dubbo.remoting.buffer.ChannelBuffer dst, int length) {
		throw new IndexOutOfBoundsException();
	}

	public void getBytes(int index,
			com.alibaba.dubbo.remoting.buffer.ChannelBuffer dst, int dstIndex,
			int length) {
		throw new IndexOutOfBoundsException();
	}

	public void getBytes(int index, OutputStream dst, int length)
			throws IOException {
		if (index < 0 || index >= total)
			throw new IndexOutOfBoundsException();
		int offset = 0;
		int count = this.components.size();
		int startIndex = this.getComponentIndex(index);
		Component b = this.components.get(startIndex);
		offset = index - b.offset;

		int len = length;
		while (startIndex < count && len > 0) {
			int l = Math.min(len, b.length - offset);
			b.buf.markReaderIndex();
			b.buf.skipBytes(offset);
			b.buf.readBytes(dst, l);
			b.buf.resetReaderIndex();
			len -= l;
			startIndex++;
			offset = 0;
			if (startIndex < count) {
				b = this.components.get(startIndex);
			}
		}
	}

	public boolean isDirect() {
		return false;
	}

	public void markReaderIndex() {
		markReaderIndex = this.readerIndex;
	}

	public void markWriterIndex() {
		throw new IllegalStateException();
	}

	public boolean readable() {
		return !this.components.isEmpty() && this.readerIndex < this.total;
	}

	public int readableBytes() {
		return this.total - this.readerIndex;
	}

	public byte readByte() {
		byte data = getByte(this.readerIndex);
		this.readerIndex++;
		return data;
	}

	public void readBytes(byte[] dst) {
		readBytes(dst, 0, dst.length);
	}

	public void readBytes(byte[] dst, int dstIndex, int length) {
		if(length == 0)return;
		int index = this.readerIndex;
		if (index < 0 || index >= total)
			throw new IndexOutOfBoundsException();
		int offset = 0;
		int count = this.components.size();
		int startIndex = this.getComponentIndex(index);
		Component b = this.components.get(startIndex);
		offset = index - b.offset;

		int len = length;
		int pos = dstIndex;
		while (startIndex < count && len > 0) {
			int l = Math.min(len, b.length - offset);
			b.buf.markReaderIndex();
			b.buf.skipBytes(offset);
			b.buf.readBytes(dst, pos, l);
			b.buf.resetReaderIndex();
			this.readerIndex += l;
			len -= l;
			pos += l;
			startIndex++;
			offset = 0;
			if (startIndex < count) {
				b = this.components.get(startIndex);
			}
		}
	}

	public void readBytes(ByteBuffer dst) {
		throw new IllegalStateException();
	}

	public void readBytes(com.alibaba.dubbo.remoting.buffer.ChannelBuffer dst) {
		throw new IllegalStateException();
	}

	public void readBytes(com.alibaba.dubbo.remoting.buffer.ChannelBuffer dst,
			int length) {
		throw new IllegalStateException();
	}

	public void readBytes(com.alibaba.dubbo.remoting.buffer.ChannelBuffer dst,
			int dstIndex, int length) {
		throw new IllegalStateException();
	}

	public com.alibaba.dubbo.remoting.buffer.ChannelBuffer readBytes(int length) {
		throw new IllegalStateException();
	}

	public void resetReaderIndex() {
		this.readerIndex = this.markReaderIndex;
	}

	public void resetWriterIndex() {
		throw new IllegalStateException();
	}

	public int readerIndex() {
		return this.readerIndex;
	}

	public void readerIndex(int readerIndex) {
		this.readerIndex = readerIndex;
	}

	public void readBytes(OutputStream dst, int length) throws IOException {
		int index = this.readerIndex;
		if (index < 0 || index >= total)
			throw new IndexOutOfBoundsException();
		int offset = 0;
		int count = this.components.size();
		int startIndex = this.getComponentIndex(index);
		Component b = this.components.get(startIndex);
		offset = index - b.offset;

		int len = length;
		while (startIndex < count && len > 0) {
			int l = Math.min(len, b.length - offset);
			b.buf.markReaderIndex();
			b.buf.skipBytes(offset);
			b.buf.readBytes(dst, l);
			b.buf.resetReaderIndex();
			this.readerIndex += l;
			len -= l;
			startIndex++;
			offset = 0;
			if (startIndex < count) {
				b = this.components.get(startIndex);
			}
		}
	}

	public void setByte(int index, int value) {
		throw new IllegalStateException();
	}

	public void setBytes(int index, byte[] src) {
		throw new IllegalStateException();
	}

	public void setBytes(int index, byte[] src, int srcIndex, int length) {
		throw new IllegalStateException();
	}

	public void setBytes(int index, ByteBuffer src) {
		throw new IllegalStateException();
	}

	public void setBytes(int index,
			com.alibaba.dubbo.remoting.buffer.ChannelBuffer src) {
		throw new IllegalStateException();
	}

	public void setBytes(int index,
			com.alibaba.dubbo.remoting.buffer.ChannelBuffer src, int length) {
		throw new IllegalStateException();
	}

	public void setBytes(int index,
			com.alibaba.dubbo.remoting.buffer.ChannelBuffer src, int srcIndex,
			int length) {
		throw new IllegalStateException();
	}

	public int setBytes(int index, InputStream src, int length)
			throws IOException {
		throw new IllegalStateException();
	}

	public void setIndex(int readerIndex, int writerIndex) {
		throw new IllegalStateException();
	}

	public void skipBytes(int length) {
		this.readerIndex += length;
	}

	public ByteBuffer toByteBuffer() {
		throw new IllegalStateException();
	}

	public ByteBuffer toByteBuffer(int index, int length) {
		throw new IllegalStateException();
	}

	public boolean writable() {
		return false;
	}

	public int writableBytes() {
		throw new IllegalStateException();
	}

	public void writeByte(int value) {
		throw new IllegalStateException();
	}

	public void writeBytes(byte[] src) {
		throw new IllegalStateException();
	}

	public void writeBytes(byte[] src, int index, int length) {
		throw new IllegalStateException();
	}

	public void writeBytes(ByteBuffer src) {
		throw new IllegalStateException();
	}

	public void writeBytes(com.alibaba.dubbo.remoting.buffer.ChannelBuffer src) {
		throw new IllegalStateException();
	}

	public void writeBytes(com.alibaba.dubbo.remoting.buffer.ChannelBuffer src,
			int length) {
		throw new IllegalStateException();
	}

	public void writeBytes(com.alibaba.dubbo.remoting.buffer.ChannelBuffer src,
			int srcIndex, int length) {
		throw new IllegalStateException();
	}

	public int writeBytes(InputStream src, int length) throws IOException {
		throw new IllegalStateException();
	}

	public int writerIndex() {
		throw new IllegalStateException();
	}

	public void writerIndex(int writerIndex) {
		throw new IllegalStateException();
	}

	public byte[] array() {
		throw new IllegalStateException();
	}

	public boolean hasArray() {
		return false;
	}

	public int arrayOffset() {
		throw new IllegalStateException();
	}

}
