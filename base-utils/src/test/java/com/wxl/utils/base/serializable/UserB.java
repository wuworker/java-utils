package com.wxl.utils.base.serializable;

import java.io.*;

/**
 * Created by wuxingle on 2017/12/2 0002.
 *
 */
public class UserB implements Externalizable{

    private static final long serialVersionUID = -8141731966107333503L;

    private String name;

    private int age;

    public UserB() {
    }

    public UserB(String name, int age) {
        this.name = name;
        this.age = age;
    }

    private Object writeReplace()throws ObjectStreamException {
        System.out.println("userb write replace");
        return this;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        System.out.println("userb start external write");
        out.writeObject(name);
        out.writeInt(age);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        System.out.println("userb start external read");

        name = (String)in.readObject();
        age = in.readInt();
    }

    private void writeObject(ObjectOutputStream out)throws IOException {
        System.out.println("userb start write object");
        out.defaultWriteObject();
    }


    private void readObject(ObjectInputStream in)throws IOException,ClassNotFoundException{
        System.out.println("userb start read object");
        in.defaultReadObject();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "UserB{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
