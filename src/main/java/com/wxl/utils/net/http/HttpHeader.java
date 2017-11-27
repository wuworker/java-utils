package com.wxl.utils.net.http;

/**
 * Created by wuxingle on 2017/7/14 0014.
 * http的头
 */
public class HttpHeader {

    private String name;

    private String value;

    public HttpHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }


    @Override
    public String toString() {
        return "HttpHeader{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }



}
