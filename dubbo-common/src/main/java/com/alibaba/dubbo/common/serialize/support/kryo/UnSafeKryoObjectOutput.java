package com.alibaba.dubbo.common.serialize.support.kryo;

import com.alibaba.dubbo.common.serialize.Cleanable;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeOutput;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Allen lei on 2015/8/31.
 */
public class UnSafeKryoObjectOutput implements ObjectOutput,Cleanable {

    private final Kryo kryo = KryoFactory.getDefaultFactory().getKryo();

    private UnsafeOutput output;

    public UnSafeKryoObjectOutput(OutputStream outputStream){
        output = new UnsafeOutput(outputStream);
    }


    public void writeObject(Object obj) throws IOException {
        kryo.writeClassAndObject(output, obj);
    }

    public void writeBool(boolean v) throws IOException {
        output.writeBoolean(v);
    }

    public void writeByte(byte v) throws IOException {
        output.writeByte(v);
    }

    public void writeShort(short v) throws IOException {
        output.writeShort(v);
    }

    public void writeInt(int v) throws IOException {
        output.writeInt(v);
    }

    public void writeLong(long v) throws IOException {
        output.writeLong(v);
    }

    public void writeFloat(float v) throws IOException {
        output.writeFloat(v);
    }

    public void writeDouble(double v) throws IOException {
        output.writeDouble(v);
    }

    public void writeUTF(String v) throws IOException {
        output.writeString(v);
    }

    public void writeBytes(byte[] v) throws IOException {
        if (v == null) {
            output.writeInt(-1);
        } else {
            writeBytes(v, 0, v.length);
        }
    }

    public void writeBytes(byte[] v, int off, int len) throws IOException {
        if (v == null) {
            output.writeInt(-1);
        } else {
            output.writeInt(len);
            output.write(v, off, len);
        }
    }

    public void flushBuffer() throws IOException {
     output.flush();
    }

    public void cleanup() {
        KryoFactory.getDefaultFactory().returnKryo(kryo);
    }
}
