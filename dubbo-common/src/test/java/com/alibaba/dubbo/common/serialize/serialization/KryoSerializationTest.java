package com.alibaba.dubbo.common.serialize.serialization;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.serialize.support.kryo.KryoSerialization;
import com.alibaba.dubbo.common.serialize.support.kryo.UnSafeKryoSerialization;
import com.esotericsoftware.kryo.Kryo;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by alei on 9/3/2015.
 */
public class KryoSerializationTest{

    Serialization serialization = new KryoSerialization();
    Serialization unsafeSerialization = new UnSafeKryoSerialization();
    URL url                   = new URL("protocl", "1.1.1.1", 1234);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    @Test
    public void test()throws Exception{
        ObjectOutput objectOutput = serialization.serialize(url, byteArrayOutputStream);

        POJOTest test = new POJOTest();
        test.setAge(23);
        test.setGendar("male");
        test.setName("test");

        objectOutput.writeObject(test);
        objectOutput.flushBuffer();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                byteArrayOutputStream.toByteArray());

        ObjectInput objectInput = serialization.deserialize(url,byteArrayInputStream);
        POJOTest pojoTest = (POJOTest)objectInput.readObject();

        Assert.assertEquals(23, pojoTest.getAge());


    }

    @Test
    public void test_StringArray_withType() throws Exception {

        String[] data = new String[] { "1", "b" };


        ObjectOutput objectOutput = serialization.serialize(url, byteArrayOutputStream);
        objectOutput.writeObject(data);
        objectOutput.flushBuffer();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                byteArrayOutputStream.toByteArray());
        ObjectInput deserialize = serialization.deserialize(url, byteArrayInputStream);

        assertArrayEquals(data, (String[]) deserialize.readObject());


    }

    @Test
    public void testUnsafe()throws Exception{
        ObjectOutput objectOutput = unsafeSerialization.serialize(url, byteArrayOutputStream);

        POJOTest test = new POJOTest();
        test.setAge(23);
        test.setGendar("male");
        test.setName("test");

        objectOutput.writeObject(test);
        objectOutput.flushBuffer();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                byteArrayOutputStream.toByteArray());

        ObjectInput objectInput = unsafeSerialization.deserialize(url,byteArrayInputStream);
        POJOTest pojoTest = objectInput.readObject(POJOTest.class);

        Assert.assertEquals(23, pojoTest.getAge());
    }


}
