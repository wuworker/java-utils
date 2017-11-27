package com.wxl.utils.net.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuxingle on 2017/7/14 0014.
 * http请求
 */
public class HttpRequested {

    private String url;

    private String method;

    //get请求参数
    private Map<String, String> params = new HashMap<>();

    //请求头
    private List<HttpHeader> headers = new ArrayList<>();

    //post请求体
    private byte[] body;

    //请求设置
    private int conTimeout = 10000;

    private int readTimeout = 10000;

    private String requestCharset = "utf-8";

    public HttpRequested() {
    }

    public HttpRequested(String url) {
        this.url = url;
    }

    public HttpRequested addParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    public String getParam(String key) {
        return params.get(key);
    }

    public Map<String, String> getAllParam() {
        return params;
    }

    public HttpRequested addHeader(String name, String value) {
        headers.add(new HttpHeader(name, value));
        return this;
    }

    public String getHeader(String name) {
        StringBuilder value = new StringBuilder();
        for (HttpHeader header : headers) {
            if (header.getName().equals(name)) {
                value.append(header.getValue())
                        .append(";");
            }
        }
        return value.length() == 0 ?
                null :
                value.substring(0, value.length() - 1);
    }

    public List<HttpHeader> getAllHeader() {
        return headers;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getConTimeout() {
        return conTimeout;
    }

    public void setConTimeout(int conTimeout) {
        this.conTimeout = conTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getRequestCharset() {
        return requestCharset;
    }

    public void setRequestCharset(String requestCharset) {
        this.requestCharset = requestCharset;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "HttpRequested{" +
                "url='" + url + '\'' +
                ", method=" + method +
                ", params=" + params +
                ", headers=" + headers +
                ", body=" + (body == null ? null : new String(body)) +
                ", conTimeout=" + conTimeout +
                ", readTimeout=" + readTimeout +
                ", requestCharset='" + requestCharset + '\'' +
                '}';
    }
}



