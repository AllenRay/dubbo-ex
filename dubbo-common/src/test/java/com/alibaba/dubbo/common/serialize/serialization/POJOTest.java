package com.alibaba.dubbo.common.serialize.serialization;

import java.io.Serializable;

/**
 * Created by alei on 9/3/2015.
 */
public class POJOTest implements Serializable {

    private int age;
    private String name;
    private String gendar;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGendar() {
        return gendar;
    }

    public void setGendar(String gendar) {
        this.gendar = gendar;
    }
}
