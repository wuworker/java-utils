package com.wxl.utils.serializable;

import java.io.*;

/**
 * Created by wuxingle on 2017/12/2 0002.
 *
 */
public class UserA extends UserC implements Serializable,ObjectInputValidation{

    private static final long serialVersionUID = -8141731966107333503L;

    private String name;

    private int age;

    private UserB userB;

    public UserA() {
    }

    public UserA(String name, int age) {
        this.name = name;
        this.age = age;
    }

    private void writeObject(ObjectOutputStream out)throws IOException{
        System.out.println("usera start write object");
        out.defaultWriteObject();
    }

    private Object writeReplace()throws ObjectStreamException {
        System.out.println("usera write replace");
        return this;
    }

    private void readObject(ObjectInputStream in)throws IOException,ClassNotFoundException{
        System.out.println("usera start read object");
        in.defaultReadObject();
        System.out.println("usera read end");
    }

    private Object readResolve()throws ObjectStreamException {
        System.out.println("usera read resolve");
        return this;
    }


    @Override
    public void validateObject() throws InvalidObjectException {
        System.out.println("validate usera");
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

    public UserB getUserB() {
        return userB;
    }

    public void setUserB(UserB userB) {
        this.userB = userB;
    }

    @Override
    public String toString() {
        return "UserA{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", userB=" + userB +
                '}';
    }
}
