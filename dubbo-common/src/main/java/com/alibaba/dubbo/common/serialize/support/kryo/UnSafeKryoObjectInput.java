package com.alibaba.dubbo.common.serialize.support.kryo;

import com.alibaba.dubbo.common.serialize.Cleanable;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeInput;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * Created by Allen lei on 2015/8/31.
 */
public class UnSafeKryoObjectInput implements ObjectInput,Cleanable{

    private final Kryo kryo = KryoFactory.getDefaultFactory().getKryo();

    private UnsafeInput input = null;

    public UnSafeKryoObjectInput(InputStream inputStream){
      input = new UnsafeInput(inputStream);
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        return kryo.readClassAndObject(input);
    }

    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        return (T)readObject();
    }

    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return readObject(cls);
    }

    public boolean readBool() throws IOException {
         return input.readBoolean();
    }

    public byte readByte() throws IOException {
        return input.readByte();
    }

    public short readShort() throws IOException {
        return input.readShort();
    }

    public int readInt() throws IOException {
        return input.readInt();
    }

    public long readLong() throws IOException {
        return input.readLong();
    }

    public float readFloat() throws IOException {
        return input.readFloat();
    }

    public double readDouble() throws IOException {
        return input.readDouble();
    }

    public String readUTF() throws IOException {
        return input.readString();
    }

    public byte[] readBytes() throws IOException {
        int len = input.readInt();
        if(len < 0){
            return null;
        }
        return input.readBytes(len);
    }
    public void cleanup() {
        KryoFactory.getDefaultFactory().returnKryo(kryo);
    }
}
