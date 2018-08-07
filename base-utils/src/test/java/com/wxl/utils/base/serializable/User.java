package com.wxl.utils.base.serializable;

import java.io.Serializable;

/**
 * Created by wuxingle on 2017/12/3 0003.
 */
public class User implements Serializable{

    private static final long serialVersionUID = 6011725086572282677L;

    private final Book book;

    private Address address;

    public User(Address address) {
        this.address = address;
        book = new Book(address);
    }

    public Address getAddress() {
        return address;
    }

    public Book getBook() {
        return book;
    }
}
