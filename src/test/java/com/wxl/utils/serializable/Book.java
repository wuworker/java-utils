package com.wxl.utils.serializable;

import java.io.Serializable;

/**
 * Created by wuxingle on 2017/12/3 0003.
 */
public class Book implements Serializable{

    private static final long serialVersionUID = -8196713066161363165L;

    private Address address;

    public Book(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }
}
