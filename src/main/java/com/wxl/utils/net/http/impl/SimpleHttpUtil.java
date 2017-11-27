package com.wxl.utils.net.http.impl;

import com.wxl.utils.net.http.HttpHeader;
import com.wxl.utils.net.http.HttpRequested;
import com.wxl.utils.net.http.HttpResponsed;
import com.wxl.utils.net.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.wxl.utils.net.http.HttpMethod.*;

/**
 * Created by wuxingle on 2017/7/14 0014.
 * 使用httpUrlConnection实现
 * 如果服务器返回500，读取输入流时会IO异常。
 */
@Slf4j
public class SimpleHttpUtil extends HttpUtil {

    //http支持方法
    private static String[] supportMethods = {
            GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE
    };

    private SimpleHttpUtil(
            String requestCharset,
            int connectTimeout,
            int readTimeout) {
        super(requestCharset, connectTimeout, readTimeout);
    }

    /**
     * 获取默认配置
     */
    public static SimpleHttpUtil createDefault() {
        return new Builder().build();
    }

    /**
     * 自定义配置
     */
    public static Builder custom() {
        return new Builder();
    }


    /**
     * 支持的请求方法
     */
    @Override
    public List<String> getSupportedMethods() {
        return Arrays.asList(supportMethods);
    }

    /**
     * 执行请求
     */
    @Override
    protected HttpResponsed doExecute(HttpRequested request, boolean useLocal) throws IOException {
        if (request.getMethod() == null) {
            throw new IllegalArgumentException("request method can not null");
        }
        //使用全局配置
        if (!useLocal) {
            setGlobalRequest(request);
        }
        //请求方法
        String method = request.getMethod().toUpperCase();
        //请求url
        URL reqUrl = new URL(buildGetUrl(request.getUrl(), request.getAllParam(), request.getRequestCharset()));
        log.debug("send requestBean url :{}", reqUrl.toString());

        HttpURLConnection urlConnection = (HttpURLConnection) reqUrl.openConnection();
        urlConnection.setDoInput(true);
        if (supportedSendBody(method)) {
            urlConnection.setDoOutput(true);
        }

        urlConnection.setRequestMethod(method);
        if (request.getConTimeout() > 0) {
            urlConnection.setConnectTimeout(request.getConTimeout());
        }
        if (request.getReadTimeout() > 0) {
            urlConnection.setReadTimeout(request.getReadTimeout());
        }
        for (HttpHeader header : request.getAllHeader()) {
            urlConnection.setRequestProperty(header.getName(), header.getValue());
        }

        urlConnection.connect();

        //支持的方法发送请求体
        if (supportedSendBody(method) && !ObjectUtils.isEmpty(request.getBody())) {
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

        byte[] data = HttpUtil.readBytes(urlConnection.getInputStream(), length);
        log.info("read body length:{}", data.length);

        httpResponsed.setBody(data);

        return httpResponsed;
    }


    /**
     * http的一些简单配置
     */
    public static class Builder extends HttpUtil.Builder {

        private Builder() {
        }

        public SimpleHttpUtil build() {
            return new SimpleHttpUtil(
                    requestCharset,
                    connectTimeout,
                    readTimeout
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

    }


}



