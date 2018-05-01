package com.wxl.utils.net.http;

import com.wxl.utils.collection.ByteArrayList;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;

import static org.springframework.http.HttpMethod.*;

/**
 * Created by wuxingle on 2017/6/23 0023.
 * http工具类
 */
public abstract class HttpUtils {

    /**
     * 把参数转为key=value,用&分隔的形式
     */
    public static <T> String toQuery(Map<String, T> query, String encode) {
        if (CollectionUtils.isEmpty(query)) {
            return "";
        }
        StringBuilder queryBuilder = new StringBuilder();
        try {
            for (Iterator<Map.Entry<String, T>> it = query.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, T> entry = it.next();
                String key = entry.getKey();
                T value = entry.getValue();
                queryBuilder.append(key == null ? "" : URLEncoder.encode(key, encode))
                        .append("=")
                        .append(value == null ? "" : URLEncoder.encode(value.toString(), encode));
                if (it.hasNext()) {
                    queryBuilder.append("&");
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedCharsetException(encode);
        }
        return queryBuilder.toString();
    }

    /**
     * 从key=value,用&分隔的形式解析为map
     */
    public static Map<String, String> fromQuery(String query, String encode) {
        if (!StringUtils.hasText(query)) {
            return new LinkedHashMap<>();
        }
        Map<String, String> map = new LinkedHashMap<>();
        try {
            for (String kvs : query.split("&")) {
                String[] kv = kvs.split("=");
                if (kv.length == 0) {
                    continue;
                }
                map.put(URLDecoder.decode(kv[0], encode),
                        kv.length > 1 ? URLDecoder.decode(kv[1], encode) : null);
            }
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedCharsetException(encode);
        }
        return map;
    }

    /**
     * 从地址中获取请求参数
     */
    public static String getQueryFromURI(String uri) {
        try {
            return new URI(uri).getQuery();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("uri not allowed:" + uri);
        }
    }

    /**
     * 构建url
     */
    public static String buildURL(String url, Map<String, String> params, String encode) {
        String query = toQuery(params, encode);
        if (!StringUtils.hasText(query)) {
            return url;
        }
        int refIndex;
        String ref = "", queryUrl = url;
        if ((refIndex = url.indexOf("#")) >= 0) {
            ref = url.substring(refIndex, url.length());
            queryUrl = url.substring(0, refIndex);
        }
        return queryUrl + (url.indexOf("?") > 0 ? "&" : "?") + query + ref;
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
        ByteArrayList list = ByteArrayList.fromStream(inputStream, length);
        return list.toByte();
    }


    /**
     * 请求默认配置
     */
    protected HttpRequestConfig requestConfig;

    protected HttpUtils(HttpRequestConfig requestConfig) {
        if (requestConfig == null) {
            requestConfig = new HttpRequestConfig();
        }
        this.requestConfig = requestConfig;
    }

    /**
     * 执行请求
     */
    protected abstract HttpResponsed doExecute(HttpRequested request, HttpRequestConfig requestConfig) throws IOException;

    /**
     * 支持的http方法
     */
    public abstract List<HttpMethod> getSupportedMethods();


    /**
     * 是否支持这个方法
     */
    public boolean supportedMethod(HttpMethod method) {
        return getSupportedMethods().contains(method);
    }

    /**
     * get请求
     *
     * @param url    url
     * @param params 参数
     * @return 响应
     */
    public byte[] doGet(String url, Map<String, String> params) throws IOException {
        HttpRequested httpRequested = new HttpRequested(url);
        httpRequested.setQuery(params);

        return doGet(httpRequested, null).getBody();
    }

    public byte[] doGet(String url) throws IOException {
        return doGet(url, null);
    }

    public HttpResponsed doGet(HttpRequested request) throws IOException {
        return doGet(request, null);
    }

    public HttpResponsed doGet(HttpRequested request, HttpRequestConfig requestConfig) throws IOException {
        Assert.notNull(request, "request can not null");
        request.setMethod(GET);
        return execute(request, requestConfig);
    }

    /**
     * head请求
     *
     * @return 请求头
     */
    public HttpHeaders doHead(String url) throws IOException {
        return doHead(new HttpRequested(url), null).getHeaders();
    }

    public HttpResponsed doHead(HttpRequested request) throws IOException {
        return doHead(request, null);
    }

    public HttpResponsed doHead(HttpRequested request, HttpRequestConfig requestConfig) throws IOException {
        Assert.notNull(request, "request can not null");
        request.setMethod(HEAD);
        return execute(request, requestConfig);
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
        httpRequested.addHeader(heads);

        return doPost(httpRequested, null).getBody();
    }

    public byte[] doPost(String url, byte[] body) throws IOException {
        return doPost(url, body, null);
    }

    public byte[] doPost(String url) throws IOException {
        return doPost(url, null);
    }

    public HttpResponsed doPost(HttpRequested request) throws IOException {
        return doPost(request, null);
    }

    public HttpResponsed doPost(HttpRequested request, HttpRequestConfig requestConfig) throws IOException {
        Assert.notNull(request, "request can not null");
        request.setMethod(POST);
        return execute(request, requestConfig);
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
        httpRequested.addHeader(heads);

        return doPut(httpRequested, null).getBody();
    }

    public byte[] doPut(String url, byte[] body) throws IOException {
        return doPut(url, body, null);
    }

    public HttpResponsed doPut(HttpRequested request) throws IOException {
        return doPut(request, null);
    }

    public HttpResponsed doPut(HttpRequested request, HttpRequestConfig requestConfig) throws IOException {
        Assert.notNull(request, "request can not null");

        request.setMethod(PUT);
        return execute(request, requestConfig);
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
        httpRequested.addHeader(heads);

        return doPatch(httpRequested, null).getBody();
    }

    public byte[] doPatch(String url, byte[] body) throws IOException {
        return doPatch(url, body, null);
    }

    public HttpResponsed doPatch(HttpRequested request) throws IOException {
        return doPatch(request, null);
    }

    public HttpResponsed doPatch(HttpRequested request, HttpRequestConfig requestConfig) throws IOException {
        Assert.notNull(request, "request can not null");

        request.setMethod(PATCH);
        return execute(request, requestConfig);
    }

    /**
     * delete请求
     */
    public byte[] doDelete(String url) throws IOException {
        HttpRequested httpRequested = new HttpRequested(url);
        return doDelete(httpRequested, null).getBody();
    }

    public HttpResponsed doDelete(HttpRequested request) throws IOException {
        return doDelete(request, null);
    }

    public HttpResponsed doDelete(HttpRequested request, HttpRequestConfig requestConfig) throws IOException {
        Assert.notNull(request, "request can not null");

        request.setMethod(DELETE);
        return execute(request, requestConfig);
    }


    /**
     * options请求
     *
     * @param url url
     */
    public List<String> doOptions(String url) throws IOException {
        HttpRequested httpRequested = new HttpRequested(url);
        HttpResponsed responsed = doOptions(httpRequested, null);
        List<String> allow = responsed.getHeaders().get("Allow");
        return allow == null ? Collections.EMPTY_LIST : allow;
    }

    public HttpResponsed doOptions(HttpRequested request) throws IOException {
        return doOptions(request, null);
    }

    public HttpResponsed doOptions(HttpRequested request, HttpRequestConfig requestConfig) throws IOException {
        Assert.notNull(request, "request can not null");

        request.setMethod(OPTIONS);
        return execute(request, requestConfig);
    }


    /**
     * 执行请求
     */
    public HttpResponsed execute(HttpRequested request, HttpRequestConfig requestConfig) throws IOException {
        Assert.notNull(request, "request can not null");
        Assert.hasLength(request.getUrl(), "request url can not null");
        Assert.isTrue(supportedMethod(request.getMethod()), "request method unsupported: " + request.getMethod());

        return doExecute(request, requestConfig);
    }

    public HttpResponsed execute(HttpRequested request) throws IOException {
        return execute(request, null);
    }


    public String getRequestCharset() {
        return requestConfig.getRequestCharset();
    }

    public int getConnectTimeout() {
        return requestConfig.getConnectTimeout();
    }

    public int getReadTimeout() {
        return requestConfig.getReadTimeout();
    }

    public boolean ignoreSSL() {
        return requestConfig.isIgnoreSSL();
    }
}

