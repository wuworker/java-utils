package com.wxl.utils.net.http;

import lombok.Data;

/**
 * Create by wuxingle on 2018/5/1
 * http请求配置
 */
@Data
public class HttpRequestConfig implements Cloneable {

    //请求的编码
    private String requestCharset = "utf-8";

    //连接超时
    private int connectTimeout = 5000;

    //读取超时
    private int readTimeout = 5000;

    //是否不验证ssl
    private boolean ignoreSSL = false;


    @Override
    public HttpRequestConfig clone() {
        try {
            return (HttpRequestConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

}
