package com.wxl.utils.net.http;

import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuxingle on 2017/7/14 0014.
 * http响应
 */
public class HttpResponsed {

    //状态
    private String statusLine = "";

    //状态码
    private int statusCode;

    //响应体
    private byte[] body;

    //响应头
    private List<HttpHeader> headers = new ArrayList<>();

    public HttpResponsed() {
    }

    public HttpResponsed addHeader(String name, String value){
        headers.add(new HttpHeader(name,value));
        return this;
    }

    public HttpResponsed addHeader(String name, List<String> values){
        if(values.isEmpty()){
            return this;
        }
        StringBuilder sb = new StringBuilder();
        for (String value:values){
            sb.append(value).append(",");
        }
        headers.add(new HttpHeader(name,sb.substring(0,sb.length() - 1)));
        return this;
    }

    public void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStringBody(String code){
        if(!StringUtils.isEmpty(body)){
            return new String(body, Charset.forName(code));
        }
        return null;
    }

    public List<HttpHeader> getHeaders() {
        return new ArrayList<>(headers);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

}

