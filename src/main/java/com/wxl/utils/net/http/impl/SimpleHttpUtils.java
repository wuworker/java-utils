package com.wxl.utils.net.http.impl;

import com.wxl.utils.annotation.ThreadSafe;
import com.wxl.utils.net.http.HttpRequestConfig;
import com.wxl.utils.net.http.HttpRequested;
import com.wxl.utils.net.http.HttpResponsed;
import com.wxl.utils.net.http.HttpUtils;
import com.wxl.utils.net.ssl.SSLUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.net.ssl.*;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.*;


/**
 * Created by wuxingle on 2017/7/14 0014.
 * 使用httpUrlConnection实现
 * 如果服务器返回500，读取输入流时会IO异常。
 */
@Slf4j
@ThreadSafe
public class SimpleHttpUtils extends HttpUtils {

    //http支持方法
    private static List<HttpMethod> supportMethods = Arrays.asList(
            GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE);

    private SSLSocketFactory ignoreSSLSocketFactory;

    private HostnameVerifier verifier;

    private SimpleHttpUtils(HttpRequestConfig requestConfig) {
        super(requestConfig);
    }

    /**
     * 获取默认配置
     */
    public static SimpleHttpUtils createDefault() {
        return new SimpleBuilder().build();
    }

    /**
     * 自定义配置
     */
    public static SimpleBuilder custom() {
        return new SimpleBuilder();
    }


    /**
     * 支持的请求方法
     */
    @Override
    public List<HttpMethod> getSupportedMethods() {
        return supportMethods;
    }

    /**
     * 执行请求
     */
    @Override
    protected HttpResponsed doExecute(HttpRequested request, HttpRequestConfig requestConfig) throws IOException {
        if (request.getMethod() == null) {
            throw new IllegalArgumentException("request method can not null");
        }
        if (requestConfig == null) {
            requestConfig = this.requestConfig;
        }
        //请求方法
        String method = request.getMethod().name();
        //请求url
        URL reqUrl = new URL(buildURL(request.getUrl(), request.getQuery(), requestConfig.getRequestCharset()));
        log.debug("send request url :{}", reqUrl.toString());

        HttpURLConnection urlConnection = (HttpURLConnection) reqUrl.openConnection();
        urlConnection.setDoInput(true);
        if (!ObjectUtils.isEmpty(request.getBody())) {
            urlConnection.setDoOutput(true);
        }

        urlConnection.setRequestMethod(method);
        if (requestConfig.getConnectTimeout() > 0) {
            urlConnection.setConnectTimeout(requestConfig.getConnectTimeout());
        }
        if (requestConfig.getReadTimeout() > 0) {
            urlConnection.setReadTimeout(requestConfig.getReadTimeout());
        }
        HttpHeaders httpHeaders = request.getHeaders();
        for (Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
            urlConnection.setRequestProperty(entry.getKey(),
                    StringUtils.collectionToDelimitedString(entry.getValue(), ";"));
        }

        //忽略ssl
        if (requestConfig.isIgnoreSSL() && urlConnection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) urlConnection).setSSLSocketFactory(getIgnoreSSLSocketFactory());
            ((HttpsURLConnection) urlConnection).setHostnameVerifier(getHostnameVerifier());
        }

        urlConnection.connect();

        //支持的方法发送请求体
        if (!ObjectUtils.isEmpty(request.getBody())) {
            //发送body
            byte[] body = request.getBody();
            try (BufferedOutputStream out = new BufferedOutputStream(
                    urlConnection.getOutputStream())) {
                out.write(body);
                out.flush();
            }
        }

        //开始获取响应
        HttpResponsed httpResponsed = new HttpResponsed();

        //设置响应头
        Map<String, List<String>> headers = urlConnection.getHeaderFields();

        httpResponsed.setStatusCode(urlConnection.getResponseCode());
        for (String key : headers.keySet()) {
            List<String> values = headers.get(key);
            log.debug("{}--->{}", key, values);
            if (key == null && !ObjectUtils.isEmpty(values)) {
                httpResponsed.setStatusLine(values.get(0));
                continue;
            }
            httpResponsed.addHeader(key, values);
        }

        int length = urlConnection.getContentLength();
        //获取响应体

        byte[] data = HttpUtils.readBytes(urlConnection.getInputStream(), length);
        log.info("read body length:{}", data.length);

        httpResponsed.setBody(data);

        return httpResponsed;
    }


    private SSLSocketFactory getIgnoreSSLSocketFactory() {
        if (ignoreSSLSocketFactory == null) {
            synchronized (this) {
                if (ignoreSSLSocketFactory == null) {
                    SSLContext sslContext = SSLUtils.getTrustAnySSLContext("TLS");
                    ignoreSSLSocketFactory = sslContext.getSocketFactory();
                }
            }
        }
        return ignoreSSLSocketFactory;
    }

    private HostnameVerifier getHostnameVerifier() {
        if (verifier == null) {
            synchronized (this) {
                if (verifier == null) {
                    verifier = (o1, o2) -> true;
                }
            }
        }
        return verifier;
    }


    /**
     * simpleHttpBuilder
     */
    public static class SimpleBuilder extends Builder<SimpleBuilder> {
        public SimpleHttpUtils build() {
            return new SimpleHttpUtils(requestConfig.clone());
        }
    }

}



