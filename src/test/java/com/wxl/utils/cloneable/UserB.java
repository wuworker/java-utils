package com.wxl.utils.cloneable;

/**
 * Created by wuxingle on 2017/12/2 0002.
 *
 */
public class UserB implements Cloneable{

    private String name;

    public UserB(String name) {
        this.name = name;
    }

    @Override
    public UserB clone() {
        UserB b = null;
        System.out.println("clone b");
        try {
            b = (UserB) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return b;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
