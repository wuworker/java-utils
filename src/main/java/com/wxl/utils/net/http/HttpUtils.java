package com.wxl.utils.net.http;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;

/**
 * Created by wuxingle on 2017/6/23 0023.
 * http工具类
 */
public abstract class HttpUtils {

    //请求的编码
    protected String requestCharset;

    //连接超时
    protected int connectTimeout;

    //读取超时
    protected int readTimeout;


    protected HttpUtils(String requestCharset,
                        int connectTimeout,
                        int readTimeout) {
        this.requestCharset = requestCharset;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }


    /**
     * 执行请求
     */
    protected abstract HttpResponsed doExecute(HttpRequested request, boolean useLocal) throws IOException;

    /**
     * 支持的http方法
     */
    public abstract List<String> getSupportedMethods();

    /**
     * 默认参数
     */
    protected static class Builder {

        protected String requestCharset = "utf-8";

        //连接超时
        protected int connectTimeout = 10000;

        //读取超时
        protected int readTimeout = 10000;

        protected Builder() {
        }
    }

    /**
     * 是否支持这个方法
     */
    public boolean supportedMethod(String method) {
        return getSupportedMethods().contains(method.toUpperCase());
    }

    /**
     * get请求
     *
     * @param url            url
     * @param params         参数
     * @param requestCharset 请求编码
     * @return 响应
     */
    public byte[] doGet(String url, Map<String, String> params, String requestCharset) throws IOException {
        HttpRequested httpRequested = new HttpRequested(url);

        httpRequested.setParams(params);

        setGlobalRequest(httpRequested);
        httpRequested.setRequestCharset(requestCharset);

        return doGet(httpRequested, true).getBody();
    }

    public byte[] doGet(String url, Map<String, String> params) throws IOException {
        return doGet(url, params, requestCharset);
    }

    public byte[] doGet(String url) throws IOException {
        return doGet(url, null);
    }

    public HttpResponsed doGet(HttpRequested request) throws IOException {
        return doGet(request,false);
    }

    public HttpResponsed doGet(HttpRequested request, boolean useLocal) throws IOException {
        Assert.notNull(request,"request can not null");
        request.setMethod(HttpMethod.GET);
        return execute(request, useLocal);
    }

    /**
     * head请求
     *
     * @return 请求头
     */
    public Map<String, String> doHead(String url) throws IOException {
        HttpRequested httpRequested = new HttpRequested(url);

        HttpResponsed responsed = doHead(httpRequested, false);
        Map<String, String> heads = new LinkedHashMap<>();
        for (HttpHeader httpHeader : responsed.getHeaders()) {
            heads.put(httpHeader.getName(), httpHeader.getValue());
        }
        return heads;
    }

    public HttpResponsed doHead(HttpRequested request) throws IOException {
        return doHead(request,false);
    }

    public HttpResponsed doHead(HttpRequested request, boolean useLocal) throws IOException {
        Assert.notNull(request,"request can not null");
        request.setMethod(HttpMethod.HEAD);
        return execute(request, useLocal);
    }


    /**
     * post请求
     *
     * @param url   url
     * @param body  请求体
     * @param heads 请求头
     * @return 响应
     */
    public byte[] doPost(String url, byte[] body, Map<String, String> heads) throws IOException {
        HttpRequested httpRequested = new HttpRequested(url);
        httpRequested.setBody(body);
        if (!ObjectUtils.isEmpty(heads)) {
            for (String key : heads.keySet()) {
                httpRequested.addHeader(key, heads.get(key));
            }
        }

        return doPost(httpRequested, false).getBody();
    }

    public byte[] doPost(String url, byte[] body) throws IOException {
        return doPost(url, body, null);
    }

    public byte[] doPost(String url) throws IOException {
        return doPost(url, null);
    }

    public HttpResponsed doPost(HttpRequested request) throws IOException {
        return doPost(request,false);
    }

    public HttpResponsed doPost(HttpRequested request, boolean useLocal) throws IOException {
        Assert.notNull(request,"request can not null");
        request.setMethod(HttpMethod.POST);
        return execute(request, useLocal);
    }

    /**
     * put请求
     *
     * @param url   url
     * @param body  请求体
     * @param heads 请求头
     * @return 响应
     */
    public byte[] doPut(String url, byte[] body, Map<String, String> heads) throws IOException {
        HttpRequested httpRequested = new HttpRequested(url);
        httpRequested.setBody(body);
        if (!ObjectUtils.isEmpty(heads)) {
            for (String key : heads.keySet()) {
                httpRequested.addHeader(key, heads.get(key));
            }
        }

        return doPut(httpRequested, false).getBody();
    }

    public byte[] doPut(String url, byte[] body) throws IOException {
        return doPut(url, body, null);
    }

    public HttpResponsed doPut(HttpRequested request) throws IOException {
        return doPut(request,false);
    }

    public HttpResponsed doPut(HttpRequested request, boolean useLocal) throws IOException {
        Assert.notNull(request,"request can not null");

        request.setMethod(HttpMethod.PUT);
        return execute(request, useLocal);
    }

    /**
     * patch请求
     *
     * @param url   url
     * @param body  请求体
     * @param heads 请求头
     * @return 响应
     */
    public byte[] doPatch(String url, byte[] body, Map<String, String> heads) throws IOException {
        HttpRequested httpRequested = new HttpRequested(url);
        httpRequested.setBody(body);
        if (!ObjectUtils.isEmpty(heads)) {
            for (String key : heads.keySet()) {
                httpRequested.addHeader(key, heads.get(key));
            }
        }

        return doPatch(httpRequested, false).getBody();
    }

    public byte[] doPatch(String url, byte[] body) throws IOException {
        return doPatch(url, body, null);
    }

    public HttpResponsed doPatch(HttpRequested request) throws IOException {
        return doPatch(request,false);
    }

    public HttpResponsed doPatch(HttpRequested request, boolean useLocal) throws IOException {
        Assert.notNull(request,"request can not null");

        request.setMethod(HttpMethod.PATCH);
        return execute(request, useLocal);
    }

    /**
     * delete请求
     */
    public byte[] doDelete(String url) throws IOException {
        HttpRequested httpRequested = new HttpRequested(url);
        return doDelete(httpRequested, true).getBody();
    }

    public HttpResponsed doDelete(HttpRequested request) throws IOException {
        return doDelete(request,false);
    }

    public HttpResponsed doDelete(HttpRequested request, boolean useLocal) throws IOException {
        Assert.notNull(request,"request can not null");

        request.setMethod(HttpMethod.DELETE);
        return execute(request, useLocal);
    }


    /**
     * options请求
     *
     * @param url url
     */
    public List<String> doOptions(String url) throws IOException {
        HttpRequested httpRequested = new HttpRequested(url);
        HttpResponsed responsed = doOptions(httpRequested, false);
        for (HttpHeader httpHeader : responsed.getHeaders()) {
            if ("Allow".equalsIgnoreCase(httpHeader.getName())) {
                String methods = httpHeader.getValue();
                return Arrays.asList(methods.trim().split(","));
            }
        }
        return new ArrayList<>();
    }

    public HttpResponsed doOptions(HttpRequested request) throws IOException {
        return doOptions(request,false);
    }

    public HttpResponsed doOptions(HttpRequested request, boolean useLocal) throws IOException {
        Assert.notNull(request,"request can not null");

        request.setMethod(HttpMethod.OPTIONS);
        return execute(request, useLocal);
    }


    /**
     * 执行请求
     */
    public HttpResponsed execute(HttpRequested request, boolean useLocal) throws IOException {
        Assert.notNull(request,"request can not null");
        Assert.hasLength(request.getUrl(),"request url can not null");
        Assert.isTrue(supportedMethod(request.getMethod()),"request method unsupported: " + request.getMethod());

        return doExecute(request, useLocal);
    }

    public HttpResponsed execute(HttpRequested request) throws IOException {
        return execute(request, false);
    }


    /**
     * 把参数转为key=value,用&分隔的形式
     */
    public static String turnParamsString(Map<String, String> params, String reqCode) {
        if (!ObjectUtils.isEmpty(params)) {
            return "";
        }
        StringBuilder reqParams = new StringBuilder();
        try {
            for (String key : params.keySet()) {
                reqParams.append(URLEncoder.encode(key, reqCode))
                        .append("=")
                        .append(URLEncoder.encode(params.get(key), reqCode))
                        .append("&");
            }
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedCharsetException(reqCode);
        }

        return reqParams.substring(0, reqParams.length() - 1);
    }

    /**
     * 构建get形式的url
     */
    public static String buildGetUrl(String url, Map<String, String> params, String reqCode) {
        StringBuilder reqParams = new StringBuilder(url);
        if (!ObjectUtils.isEmpty(params)) {
            reqParams.append("?").append(turnParamsString(params, reqCode));
        }
        return reqParams.toString();
    }


    /**
     * 读取输入流转成byte
     */
    public static byte[] readBytes(InputStream in) throws IOException {
        return readBytes(in, 4096);
    }

    public static byte[] readBytes(InputStream inputStream, int length) throws IOException {
        if (length < 0) {
            length = 4096;
        }
        byte[] data = new byte[length];
        int offset = 0;
        int len;
        try (BufferedInputStream in = new BufferedInputStream(inputStream)) {
            if (data.length == 0) {
                return data;
            }
            while ((len = in.read(data, offset, data.length - offset)) != -1) {
                offset += len;
                if (offset == data.length) {
                    byte[] tmp = new byte[data.length * 3 / 2];
                    System.arraycopy(data, 0, tmp, 0, data.length);
                    data = tmp;
                }
            }
        }
        return Arrays.copyOf(data, offset);
    }

    /**
     * 设置全局参数
     */
    protected void setGlobalRequest(HttpRequested request) {
        request.setReadTimeout(readTimeout);
        request.setConTimeout(connectTimeout);
        request.setRequestCharset(requestCharset);
    }

    public String getRequestCharset() {
        return requestCharset;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }
}

