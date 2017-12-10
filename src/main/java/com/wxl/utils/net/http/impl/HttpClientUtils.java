package com.wxl.utils.net.http.impl;


import com.wxl.utils.annotation.ThreadSafe;
import com.wxl.utils.net.http.HttpHeader;
import com.wxl.utils.net.http.HttpRequested;
import com.wxl.utils.net.http.HttpResponsed;
import com.wxl.utils.net.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.wxl.utils.net.http.HttpMethod.*;

/**
 * Created by wuxingle on 2017/7/15 0015.
 * 使用apache的httpClient实现
 */
@Slf4j
@ThreadSafe
public class HttpClientUtils extends HttpUtils {

    private CloseableHttpClient httpClient;

    //从连接池获取连接超时时间
    private int conRequestTimeout;

    //最大线程数
    private int maxThread;

    //对相同host请求的最大线程数
    private int maxRouteThread;

    //keepAlive(毫秒)
    //优先用服务器发送的keepAlive时间
    private int defaultKeepAlive;

    //http支持方法
    private static String[] supportMethods = {
            GET, HEAD, POST, PUT, PATCH,DELETE, OPTIONS, TRACE
    };


    private HttpClientUtils(
            String requestCharset,
            int connectTimeout,
            int readTimeout,
            int conRequestTimeout,
            int maxThread,
            int maxRouteThread,
            int defaultKeepAlive) {
        super(requestCharset, connectTimeout, readTimeout);
        this.conRequestTimeout = conRequestTimeout;
        this.maxThread = maxThread;
        this.maxRouteThread = maxRouteThread;
        this.defaultKeepAlive = defaultKeepAlive;

        //请求配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(conRequestTimeout)
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(readTimeout)
                .build();
        //连接池配置
        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(maxRouteThread);
        connectionManager.setMaxTotal(maxThread);

        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
                    @Override
                    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                        long keepAlive = super.getKeepAliveDuration(response, context);
                        if (keepAlive == -1) {
                            keepAlive = defaultKeepAlive;
                        }
                        return keepAlive;
                    }
                }).build();
    }

    /**
     * 获取默认实例
     */
    public static HttpClientUtils createDefault() {
        return new Builder().build();
    }

    /**
     * 自定义参数
     */
    public static Builder custom() {
        return new Builder();
    }


    /**
     * 执行请求
     */
    @Override
    protected HttpResponsed doExecute(HttpRequested request, boolean useLocal) throws IOException {
        HttpRequestBase requestBase = getHttpRequestBase(request, useLocal);

        HttpResponsed httpResponsed = new HttpResponsed();
        try (CloseableHttpResponse response = httpClient.execute(requestBase)) {
            log.debug("server status: {}", response.getStatusLine());
            httpResponsed.setStatusLine(response.getStatusLine().toString());
            httpResponsed.setStatusCode(response.getStatusLine().getStatusCode());

            //打印head
            Header[] headers = response.getAllHeaders();
            for (Header h : headers) {
                log.debug("{}--->{}", h.getName(), h.getValue());
                httpResponsed.addHeader(h.getName(), h.getValue());
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                byte[] data = EntityUtils.toByteArray(entity);
                EntityUtils.consume(entity);
                httpResponsed.setBody(data);
            } else {
                log.info("the response entity is null");
            }
        }
        return httpResponsed;
    }


    /**
     * 所有支持的http方法
     */
    @Override
    public List<String> getSupportedMethods() {
        return Arrays.asList(supportMethods);
    }

    /**
     * 获取相应的请求
     */
    private HttpRequestBase getHttpRequestBase(HttpRequested request, boolean useLocal) {
        HttpRequestBase requestBase = null;
        String url = buildGetUrl(request.getUrl(), request.getAllParam(),
                useLocal ? request.getRequestCharset() : requestCharset);
        switch (request.getMethod().toUpperCase()) {
            case GET:
                requestBase = new HttpGet(url);
                break;
            case HEAD:
                requestBase = new HttpHead(url);
                break;
            case POST:
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new ByteArrayEntity(request.getBody()));
                requestBase = httpPost;
                break;
            case PUT:
                HttpPut httpPut = new HttpPut(url);
                httpPut.setEntity(new ByteArrayEntity(request.getBody()));
                requestBase = httpPut;
                break;
            case PATCH:
                HttpPatch httpPatch = new HttpPatch(url);
                httpPatch.setEntity(new ByteArrayEntity(request.getBody()));
                requestBase = httpPatch;
                break;
            case DELETE:
                requestBase = new HttpDelete(url);
                break;
            case OPTIONS:
                requestBase = new HttpOptions(url);
                break;
            case TRACE:
                requestBase = new HttpTrace(url);
                break;
            default:
                throw new UnsupportedOperationException("unsupported request method: " + request.getMethod());
        }

        //使用本地配置
        if (useLocal) {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(request.getReadTimeout())
                    .setConnectTimeout(request.getConTimeout())
                    .build();
            requestBase.setConfig(requestConfig);
        }
        //设置请求头
        for (HttpHeader header : request.getAllHeader()) {
            requestBase.setHeader(header.getName(), header.getValue());
        }

        return requestBase;
    }


    /**
     * 释放最后的连接
     * 该实例无法再使用
     */
    public void release() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    /**
     * httpClient的一些配置
     */
    public static class Builder extends HttpUtils.Builder {
        //从连接池获取连接超时时间
        private int conRequestTimeout = 10000;
        //最大线程数
        private int maxThread = 50;
        //对相同host请求的最大线程数
        private int maxRouteThread = 20;
        //keepAlive(毫秒)
        //优先用服务器发送的keepAlive时间
        private int defaultKeepAlive = 10 * 1000;

        private Builder() {
        }

        public HttpClientUtils build() {
            return new HttpClientUtils(
                    requestCharset,
                    connectTimeout,
                    readTimeout,
                    conRequestTimeout,
                    maxThread,
                    maxRouteThread,
                    defaultKeepAlive
            );
        }

        public Builder setRequestCharset(String requestCharset) {
            this.requestCharset = requestCharset;
            return this;
        }

        public Builder setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder setConRequestTimeout(int conRequestTimeout) {
            this.conRequestTimeout = conRequestTimeout;
            return this;
        }

        public Builder setMaxThread(int maxThread) {
            this.maxThread = maxThread;
            return this;
        }

        public Builder setMaxRouteThread(int maxRouteThread) {
            this.maxRouteThread = maxRouteThread;
            return this;
        }

        public Builder setDefaultKeepAlive(int defaultKeepAlive) {
            this.defaultKeepAlive = defaultKeepAlive;
            return this;
        }
    }

    public int getConRequestTimeout() {
        return conRequestTimeout;
    }

    public int getMaxThread() {
        return maxThread;
    }

    public int getMaxRouteThread() {
        return maxRouteThread;
    }

    public int getDefaultKeepAlive() {
        return defaultKeepAlive;
    }


}



