package com.alibaba.dubbo.common.serialize.support.kryo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Allen lei on 2015/8/31.
 */
public class UnSafeKryoSerialization implements Serialization {

    public byte getContentTypeId() {
        return 9;
    }

    public String getContentType() {
        return "x-application/unsafeKryo";
    }

    public ObjectOutput serialize(URL url, OutputStream output) throws IOException {
        return new UnSafeKryoObjectOutput(output);
    }

    public ObjectInput deserialize(URL url, InputStream input) throws IOException {
        return new UnSafeKryoObjectInput(input);
    }
}
