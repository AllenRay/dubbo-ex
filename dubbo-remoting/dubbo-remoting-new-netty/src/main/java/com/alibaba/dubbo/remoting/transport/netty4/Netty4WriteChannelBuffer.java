package com.alibaba.dubbo.remoting.transport.netty4;

import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * todo: binary-serach to 
 * @author ruanjunbo
 * @qq  33224696
 * 
 *
 */
public class Netty4WriteChannelBuffer implements com.alibaba.dubbo.remoting.buffer.ChannelBuffer {

	ByteBufAllocator alloc;
	int writerIndex = 0;
	int markWriterIndex = 0;
	List<Component> components;
	int total;
	int bufferSize = 1024;
	public Netty4WriteChannelBuffer(){
		//read bufferSize;
		//TODO initial from configurator
		bufferSize = 1024;
		components = new ArrayList<Component>();
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

	public void initialize(ByteBufAllocator alloc){
		this.clear();
		this.alloc = alloc;
		ByteBuf b = alloc.buffer(bufferSize);
		Component c = new Component();
		c.buf = b;
		c.offset = 0;
		c.length = b.capacity();
		c.endOffset = c.offset + c.length;
		this.components.add(c);
		this.total = b.capacity();
	}

	public ByteBuf toByteBuf() {
		int count = this.components.size();
		for(int i = count - 1;i > -1;i--){
			Component b = this.components.get(i);
			if(b.buf.writerIndex() == 0){
				this.components.remove(i);
				b.buf.release();
			}else{
				break;
			}
		}
		if(this.components.size() == 1){
			Component b = this.components.get(0);
			this.components.clear();
			this.total = 0;
			this.writerIndex = 0;
			return b.buf;
		}

		List<ByteBuf> bs = new ArrayList<ByteBuf>();
		for(Component b : this.components){
			bs.add(b.buf);
		}
		CompositeByteBuf cb = new CompositeByteBuf(this.alloc, false, this.components.size(), bs);

		this.components.clear();
		this.total = 0;
		this.writerIndex = 0;
		return cb;
	}

	public int compareTo(ChannelBuffer arg0) {
		throw new IllegalStateException();
	}

	public int capacity() {
		return total;
	}

	public void clear() {
		if(this.components != null){
			while(!components.isEmpty()){
				Component b = components.remove(0);
				b.buf.release();
			}
		}
		this.total = 0;
		this.writerIndex = 0;
		this.markWriterIndex = 0;
		this.alloc = null;
	}

	public ChannelBuffer copy() {
		throw new IllegalStateException();
	}

	public ChannelBuffer copy(int index, int length) {
		throw new IllegalStateException();
	}

	public void discardReadBytes() {
		throw new IllegalStateException();
	}

	public void ensureWritableBytes(int writableBytes) {
		int w = writableBytes();
		if(w < writableBytes){
			int len = (writableBytes - w + bufferSize-1)/bufferSize;
			len *= bufferSize;
			ByteBuf b = alloc.ioBuffer(len);
			this.total += b.capacity();
			Component c = new Component();
			c.buf = b;
			c.length = b.capacity();
			if(this.components.isEmpty()){
				c.offset = 0;
			}else{
				c.offset = this.components.get(this.components.size() -1).endOffset;
			}
			c.endOffset = c.offset + c.length;
			this.components.add(c);
		}
	}

	public ChannelBufferFactory factory() {
		return null;
	}

	public byte getByte(int index) {
		throw new IllegalStateException();
	}

	public void getBytes(int index, byte[] dst) {
		throw new IllegalStateException();
	}

	public void getBytes(int index, byte[] dst, int dstIndex, int length) {
		throw new IllegalStateException();
	}

	public void getBytes(int index, ByteBuffer dst) {
		throw new IllegalStateException();
	}

	public void getBytes(int index, ChannelBuffer dst) {
		throw new IllegalStateException();
	}

	public void getBytes(int index, ChannelBuffer dst, int length) {
		throw new IllegalStateException();
	}

	public void getBytes(int index, ChannelBuffer dst, int dstIndex, int length) {
		throw new IllegalStateException();
	}

	public void getBytes(int index, OutputStream dst, int length)
			throws IOException {
		throw new IllegalStateException();
	}

	public boolean isDirect() {
		return false;
	}

	public void markReaderIndex() {
		throw new IllegalStateException();
	}

	public void markWriterIndex() {
		this.markWriterIndex = this.writerIndex;
	}

	public boolean readable() {
		return false;
	}

	public int readableBytes() {
		return this.writerIndex;
	}

	public byte readByte() {
		throw new IllegalStateException();
	}

	public void readBytes(byte[] dst) {
		throw new IllegalStateException();
	}

	public void readBytes(byte[] dst, int dstIndex, int length) {
		throw new IllegalStateException();
	}

	public void readBytes(ByteBuffer dst) {
		throw new IllegalStateException();
	}

	public void readBytes(ChannelBuffer dst) {
		throw new IllegalStateException();
	}

	public void readBytes(ChannelBuffer dst, int length) {
		throw new IllegalStateException();
	}

	public void readBytes(ChannelBuffer dst, int dstIndex, int length) {
		throw new IllegalStateException();
	}

	public ChannelBuffer readBytes(int length) {
		throw new IllegalStateException();
	}

	public void resetReaderIndex() {
		throw new IllegalStateException();
	}

	public void resetWriterIndex() {
		this.writerIndex = this.markWriterIndex;
	}

	public int readerIndex() {
		throw new IllegalStateException();
	}

	public void readerIndex(int readerIndex) {
		throw new IllegalStateException();
	}

	public void readBytes(OutputStream dst, int length) throws IOException {
		throw new IllegalStateException();
	}

	public void setByte(int index, int value) {
		if(index < 0 || index >= total) throw new IndexOutOfBoundsException();
		Component c = this.getComponent(index);
		int off = index - c.offset;
		c.buf.setByte(off, value);
	}

	public void setBytes(int index, byte[] src) {
		this.setBytes(index, src, 0, src.length);
	}

	public void setBytes(int index, byte[] src, int srcIndex, int length) {
		int startIndex = this.getComponentIndex(index);
		Component c = this.components.get(startIndex);
		int count = this.components.size();
		int off = index - c.offset;
		int len = length;
		int pos = srcIndex;
		while(len > 0 && startIndex < count){
			int l = Math.min(c.length - off, len);
			c.buf.setBytes(off, src, pos, l);
			pos += l;
			len -= l;
			off = 0;
			startIndex ++;
			if(startIndex < count) c = this.components.get(startIndex);
		}
	}

	public void setBytes(int index, ByteBuffer src) {
		throw new IllegalStateException();
	}

	public void setBytes(int index, ChannelBuffer src) {
		throw new IllegalStateException();
	}

	public void setBytes(int index, ChannelBuffer src, int length) {
		throw new IllegalStateException();
	}

	public void setBytes(int index, ChannelBuffer src, int srcIndex, int length) {
		throw new IllegalStateException();
	}

	public int setBytes(int index, InputStream src, int length)
			throws IOException {
		int startIndex = this.getComponentIndex(index);
		Component c = this.components.get(startIndex);
		int count = this.components.size();
		int off = index - c.offset;
		int len = length;
		int sum = 0;
		while(len > 0 && startIndex < count){
			int l = Math.min(c.length - off, len);
			int w = c.buf.setBytes(off, src, l);
			sum += w;
			len -= w;
			off = 0;
			startIndex ++;
			if(startIndex < count) c = this.components.get(startIndex);
		}
		return sum;
	}

	public void setIndex(int readerIndex, int writerIndex) {
		throw new IllegalStateException();
	}

	public void skipBytes(int length) {
		throw new IllegalStateException();
	}

	public ByteBuffer toByteBuffer() {
		if(this.components.size() == 1){
			return this.components.get(0).buf.nioBuffer();
		}
		List<ByteBuf> bs = new ArrayList<ByteBuf>();
		for(Component c : this.components){
			bs.add(c.buf);
		}
		CompositeByteBuf cb = new CompositeByteBuf(this.alloc, false, this.components.size(), bs);
		return cb.nioBuffer();
	}

	public ByteBuffer toByteBuffer(int index, int length) {
		throw new IllegalStateException();
	}

	public boolean writable() {
		return true;
	}

	public int writableBytes() {
		return this.total - this.writerIndex;
	}

	public void writeByte(int value) {
		ensureWritableBytes(1);
		int startIndex = this.getComponentIndex(this.writerIndex);
		Component c = this.components.get(startIndex);
		int off = this.writerIndex - c.offset;

		c.buf.writerIndex(off);
		c.buf.writeByte(value);
		this.writerIndex ++;
	}

	public void writeBytes(byte[] src) {
		writeBytes(src, 0, src.length);
	}

	public void writeBytes(byte[] src, int srcIndex, int length) {
		ensureWritableBytes(length);
		int startIndex = this.getComponentIndex(this.writerIndex);
		Component c = this.components.get(startIndex);
		int off = this.writerIndex - c.offset;
		int count = this.components.size();

		int len = length;
		int pos = srcIndex;
		while(len > 0 && startIndex < count){
			int l = Math.min(c.length - off, len);
			c.buf.writerIndex(off);
			c.buf.writeBytes(src, pos, l);
			pos += l;
			len -= l;
			off = 0;
			startIndex ++;
			this.writerIndex += l;
			if(startIndex < count) c = this.components.get(startIndex);
		}
	}

	public void writeBytes(ByteBuffer src) {
		throw new IllegalStateException();
	}

	public void writeBytes(ChannelBuffer src) {
		throw new IllegalStateException();
	}

	public void writeBytes(ChannelBuffer src, int length) {
		throw new IllegalStateException();
	}

	public void writeBytes(ChannelBuffer src, int srcIndex, int length) {
		throw new IllegalStateException();
	}

	public int writeBytes(InputStream src, int length) throws IOException {
		ensureWritableBytes(length);
		int startIndex = this.getComponentIndex(this.writerIndex);
		Component c = this.components.get(startIndex);
		int off = this.writerIndex - c.offset;
		int count = this.components.size();

		int len = length;
		int sum = 0;
		while(len > 0 && startIndex < count){
			int l = Math.min(c.length - off, len);
			c.buf.writerIndex(off);
			int w = c.buf.writeBytes(src, l);
			sum += w;
			len -= w;
			off = 0;
			startIndex ++;
			this.writerIndex += w;
			if(startIndex < count) c = this.components.get(startIndex);
		}
		return sum;
	}

	public int writerIndex() {
		return this.writerIndex;
	}

	public void writerIndex(int writerIndex) {
		if(this.writerIndex != writerIndex){
			this.writerIndex = writerIndex;
			this.ensureWritableBytes(0);
			//map to byteBuf
			int startIndex = this.getComponentIndex(this.writerIndex);
			Component c = null;
			int off = 0;
			if(startIndex < 0){
				startIndex = this.components.size() -1;
				c = this.components.get(startIndex);
				off = c.length;
			}else{
				c = this.components.get(startIndex);
				off = this.writerIndex - c.offset;
			}
			int count = this.components.size();

			c.buf.writerIndex(off);
			off = startIndex + 1;
			while(off < count){
				c = this.components.get(off);
				c.buf.writerIndex(0);
				off ++;
			}
			startIndex --;
			while(startIndex > -1){
				c = this.components.get(startIndex);
				c.buf.writerIndex(c.length);
				startIndex --;
			}
		}
	}

	public byte[] array() {
		throw new IllegalStateException();
	}

	public boolean hasArray() {
		return false;
	}

	public int arrayOffset() {
		return 0;
	}
	 
}
