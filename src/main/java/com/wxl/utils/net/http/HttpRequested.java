package com.wxl.utils.net.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by wuxingle on 2017/7/14 0014.
 * http请求
 */
@Setter
@Getter
public class HttpRequested {

    private String url;

    private HttpMethod method;

    //get请求参数
    private Map<String, String> query = new HashMap<>();

    //请求头
    private HttpHeaders headers = new HttpHeaders();

    //post请求体
    private byte[] body;

    public HttpRequested() {
    }

    public HttpRequested(String url) {
        this.url = url;
    }

    public void addQuery(String key, String value) {
        query.put(key, value);
    }

    public void addQuery(Map<String, String> params) {
        if (!CollectionUtils.isEmpty(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                query.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public String getQuery(String key) {
        return query.get(key);
    }

    public void addHeader(String name, String value) {
        headers.add(name, value);
    }

    public void addHeader(Map<String, String> header) {
        if (!CollectionUtils.isEmpty(header)) {
            for (Map.Entry<String, String> h : header.entrySet()) {
                headers.add(h.getKey(), h.getValue());
            }
        }
    }

    public List<String> getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public String toString() {
        return "{" +
                "url='" + url + '\'' +
                ", method=" + method +
                ", query=" + query +
                ", headers=" + headers +
                ", body=" + new String(body) +
                '}';
    }
}



