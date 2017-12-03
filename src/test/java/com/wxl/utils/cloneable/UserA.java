package com.wxl.utils.cloneable;

/**
 * Created by wuxingle on 2017/12/2 0002.
 */
public class UserA implements Cloneable {

    private String name;

    private int age;

    private final UserB userB;

    public UserA(String name, int age,UserB userB) {
        this.name = name;
        this.age = age;
        this.userB = userB;
    }

    @Override
    public UserA clone() {
        UserA a = null;
        System.out.println("clone a");
        try {
            a = (UserA) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return a;
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


}
