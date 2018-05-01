package com.wxl.utils.net.http.impl;

import com.wxl.utils.net.http.HttpRequestConfig;
import com.wxl.utils.net.http.HttpRequested;
import com.wxl.utils.net.http.HttpResponsed;
import com.wxl.utils.net.http.HttpUtils;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuxingle on 2017/7/15 0015.
 *
 */
public class HttpClientUtilTest {

    @Test
    public void doGet() throws Exception {
        HttpUtils httpUtils = HttpClientUtils.custom()
                .setConnectTimeout(10000)
                .setConRequestTimeout(10000)
                .setReadTimeout(10000)
                .build();

        HttpRequested requested = new HttpRequested("http://localhost:8888/simple/all");
        requested.addQuery("name","哈哈");
        requested.addQuery("age","23");
        HttpResponsed responsed = httpUtils.doGet(requested);

        System.out.println(responsed.getStringBody("utf-8"));
    }

    @Test
    public void doHead()throws Exception{
        HttpUtils httpUtils = HttpClientUtils.createDefault();

        HttpHeaders heads = httpUtils.doHead("http://localhost:8888/simple/all");

        System.out.println(heads);
    }

    @Test
    public void doPOST()throws Exception{
        HttpUtils httpUtils = HttpClientUtils.createDefault();

        String body = "{\"name\":\"hahahha\",\"age\":303}";

        Map<String,String> heads = new HashMap<>();
        heads.put("Content-Type","application/json");

        byte[] res = httpUtils.doPost("http://localhost:8888/simple/json",body.getBytes(),heads);

        System.out.println(new String(res));
    }

    @Test
    public void doPUT()throws Exception{
        HttpUtils httpUtils = HttpClientUtils.createDefault();

        String body = "{\"name\":\"hahahha\",\"age\":303}";

        Map<String,String> heads = new HashMap<>();
        heads.put("Content-Type","application/json");

        byte[] res = httpUtils.doPut("http://localhost:8888/simple/json",body.getBytes(),heads);

        System.out.println(new String(res));
    }

    @Test
    public void doPATCH()throws Exception{
        HttpUtils httpUtils = HttpClientUtils.createDefault();

        String body = "{\"name\":\"hahahha\",\"age\":303}";

        Map<String,String> heads = new HashMap<>();
        heads.put("Content-Type","application/json");

        byte[] res = httpUtils.doPatch("http://localhost:8888/simple/json",body.getBytes(),heads);

        System.out.println(new String(res));
    }

    @Test
    public void doDelete()throws Exception{
        HttpUtils httpUtils = HttpClientUtils.createDefault();

        byte[] res  = httpUtils.doDelete("http://localhost:8888/simple/all");

        System.out.println(new String(res));
    }

    @Test
    public void doOptions()throws Exception{
        HttpUtils httpUtils = HttpClientUtils.createDefault();

        List<String> methods = httpUtils.doOptions("http://localhost:8888/simple/all");

        System.out.println(methods);
    }

    @Test
    public void doTrace()throws Exception{
        HttpUtils httpUtils = HttpClientUtils.createDefault();
        HttpRequested requested = new HttpRequested("https://www.baidu.com/");
        requested.setMethod(HttpMethod.TRACE);

        HttpResponsed responsed = httpUtils.execute(requested);

        System.out.println(responsed.getStatusLine());

    }


    @Test
    public void doGetHttps() throws IOException {
        HttpRequested requested = new HttpRequested("https://127.0.0.1:8443");
        HttpRequestConfig requestConfig = new HttpRequestConfig();
        requestConfig.setIgnoreSSL(true);
        HttpResponsed responsed = HttpClientUtils.createDefault().doGet(requested, requestConfig);
        System.out.println(responsed.getStringBody("utf-8"));
    }

}