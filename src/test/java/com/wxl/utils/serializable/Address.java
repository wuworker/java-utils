package com.wxl.utils.serializable;

import java.io.Serializable;

/**
 * Created by wuxingle on 2017/12/3 0003.
 */
public class Address implements Serializable{

    private static final long serialVersionUID = -4540127607686756615L;
    private String name;

    public Address(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
