package com.wxl.utils.net.http.impl;


import com.wxl.utils.annotation.ThreadSafe;
import com.wxl.utils.net.http.HttpRequestConfig;
import com.wxl.utils.net.http.HttpRequested;
import com.wxl.utils.net.http.HttpResponsed;
import com.wxl.utils.net.http.HttpUtils;
import com.wxl.utils.net.ssl.SSLUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.*;


/**
 * Created by wuxingle on 2017/7/15 0015.
 * 使用apache的httpClient实现
 */
@Slf4j
@ThreadSafe
public class HttpClientUtils extends HttpUtils {

    //http支持方法
    private static List<HttpMethod> supportMethods = Arrays.asList(
            GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE);


    @Getter
    @Setter
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    private static class HttpClientConfig extends HttpRequestConfig {
        //从连接池获取连接超时时间
        private int conRequestTimeout = 5000;
        //最大线程数
        private int maxThread = 20;
        //对相同host请求的最大线程数
        private int maxRouteThread = 5;
        //keepAlive(毫秒)
        //优先用服务器发送的keepAlive时间
        private int defaultKeepAlive = 30000;

        @Override
        public HttpClientConfig clone() {
            return (HttpClientConfig) super.clone();
        }
    }

    private CloseableHttpClient httpClient;

    private HttpClientUtils(HttpClientConfig clientConfig) {
        super(clientConfig);

        //请求配置
        RequestConfig rc = RequestConfig.custom()
                .setConnectionRequestTimeout(clientConfig.getConRequestTimeout())
                .setConnectTimeout(requestConfig.getConnectTimeout())
                .setSocketTimeout(requestConfig.getReadTimeout())
                .build();
        //连接池配置
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                //不验证ssl
                .register("ihttps", new SSLConnectionSocketFactory(SSLUtils.getTrustAnySSLContext("TLS"), (o1, o2) -> true))
                .build();

        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager(registry);
        connectionManager.setDefaultMaxPerRoute(clientConfig.getMaxRouteThread());
        connectionManager.setMaxTotal(clientConfig.getMaxThread());
        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(rc)
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
                    @Override
                    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                        long keepAlive = super.getKeepAliveDuration(response, context);
                        if (keepAlive == -1) {
                            keepAlive = clientConfig.getDefaultKeepAlive();
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
    protected HttpResponsed doExecute(HttpRequested request, HttpRequestConfig requestConfig) throws IOException {
        HttpRequestBase requestBase = getHttpRequestBase(request, requestConfig);

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
    public List<HttpMethod> getSupportedMethods() {
        return supportMethods;
    }

    /**
     * 获取相应的请求
     */
    private HttpRequestBase getHttpRequestBase(HttpRequested request, HttpRequestConfig requestConfig) {
        HttpRequestBase requestBase;
        String url = buildURL(request.getUrl(), request.getQuery(), requestConfig == null ?
                this.requestConfig.getRequestCharset() : requestConfig.getRequestCharset());
        //忽略ssl验证
        if ((requestConfig == null ? this.requestConfig.isIgnoreSSL() : requestConfig.isIgnoreSSL())
                && url.startsWith("https")) {
            url = "i" + url;
        }
        switch (request.getMethod()) {
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
        if (requestConfig != null) {
            RequestConfig rc = RequestConfig.custom()
                    .setSocketTimeout(requestConfig.getReadTimeout())
                    .setConnectTimeout(requestConfig.getConnectTimeout())
                    .build();
            requestBase.setConfig(rc);
        }
        //设置请求头
        for (Map.Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
            requestBase.setHeader(entry.getKey(),
                    StringUtils.collectionToDelimitedString(entry.getValue(), ";"));
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
    public static class Builder {
        private HttpClientConfig clientConfig;

        private Builder() {
            clientConfig = new HttpClientConfig();
        }

        public HttpClientUtils build() {
            return new HttpClientUtils(clientConfig.clone());
        }

        public Builder setRequestCharset(String requestCharset) {
            clientConfig.setRequestCharset(requestCharset);
            return this;
        }

        public Builder setConnectTimeout(int connectTimeout) {
            clientConfig.setConnectTimeout(connectTimeout);
            return this;
        }

        public Builder setReadTimeout(int readTimeout) {
            clientConfig.setReadTimeout(readTimeout);
            return this;
        }

        public Builder setIgnoreSSL(boolean ignore) {
            clientConfig.setIgnoreSSL(ignore);
            return this;
        }

        public Builder setConRequestTimeout(int conRequestTimeout) {
            clientConfig.setConRequestTimeout(conRequestTimeout);
            return this;
        }

        public Builder setMaxThread(int maxThread) {
            clientConfig.setMaxThread(maxThread);
            return this;
        }

        public Builder setMaxRouteThread(int maxRouteThread) {
            clientConfig.setMaxRouteThread(maxRouteThread);
            return this;
        }

        public Builder setDefaultKeepAlive(int defaultKeepAlive) {
            clientConfig.setDefaultKeepAlive(defaultKeepAlive);
            return this;
        }
    }

    public int getConRequestTimeout() {
        return ((HttpClientConfig) requestConfig).getConRequestTimeout();
    }

    public int getMaxThread() {
        return ((HttpClientConfig) requestConfig).getMaxThread();
    }

    public int getMaxRouteThread() {
        return ((HttpClientConfig) requestConfig).getMaxRouteThread();
    }

    public int getDefaultKeepAlive() {
        return ((HttpClientConfig) requestConfig).getDefaultKeepAlive();
    }


}



