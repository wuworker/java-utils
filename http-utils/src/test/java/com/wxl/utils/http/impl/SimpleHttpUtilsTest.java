package com.wxl.utils.http.impl;


import com.wxl.utils.http.HttpRequestConfig;
import com.wxl.utils.http.HttpRequested;
import com.wxl.utils.http.HttpResponsed;
import com.wxl.utils.http.HttpUtils;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuxingle on 2017/7/14 0014.
 */
public class SimpleHttpUtilsTest {


    @Test
    public void doGet() throws Exception {
        HttpUtils httpUtils = SimpleHttpUtils.custom()
                .setConnectTimeout(10000)
                .setReadTimeout(10000)
                .build();
        Map<String, String> params = new HashMap<>();
        params.put("name", "哈哈");
        params.put("age", "23");

        //byte[] res = httpUtils.doGet("http://localhost:8888/simple/all",params);

        HttpRequested requested = new HttpRequested("http://localhost:8888/simple/all");
        requested.addQuery("name", "哈哈");
        requested.addQuery("age", "23");
        HttpResponsed responsed = httpUtils.doGet(requested);

        System.out.println(responsed.getStringBody("utf-8"));
    }

    @Test
    public void doHead() throws Exception {
        HttpUtils httpUtils = SimpleHttpUtils.custom()
                .setConnectTimeout(2000)
                .setReadTimeout(2000)
                .build();

        HttpHeaders heads = httpUtils.doHead("http://localhost:8888/simple/all");

        System.out.println(heads);
    }


    @Test
    public void doPOST() throws Exception {
        HttpUtils httpUtils = SimpleHttpUtils.createDefault();

        String body = "{\"name\":\"hahahha\",\"age\":303}";

        Map<String, String> heads = new HashMap<>();
        heads.put("Content-Type", "application/json");

        byte[] res = httpUtils.doPost("http://localhost:8888/simple/json", body.getBytes(), heads);

        System.out.println(new String(res));
    }


    @Test
    public void doPUT() throws Exception {
        HttpUtils httpUtils = SimpleHttpUtils.createDefault();

        String body = "{\"name\":\"hahahha\",\"age\":303}";

        Map<String, String> heads = new HashMap<>();
        heads.put("Content-Type", "application/json");

        byte[] res = httpUtils.doPut("http://localhost:8888/simple/json", body.getBytes(), heads);

        System.out.println(new String(res));
    }

    @Test
    public void doPATCH() throws Exception {
        HttpUtils httpUtils = SimpleHttpUtils.createDefault();

        String body = "{\"name\":\"hahahha\",\"age\":303}";

        Map<String, String> heads = new HashMap<>();
        heads.put("Content-Type", "application/json");

        byte[] res = httpUtils.doPatch("http://localhost:8888/simple/json", body.getBytes(), heads);

        System.out.println(new String(res));
    }

    @Test
    public void doDelete() throws Exception {
        HttpUtils httpUtils = SimpleHttpUtils.createDefault();

        byte[] res = httpUtils.doDelete("http://localhost:8888/simple/all");

        System.out.println(new String(res));
    }


    @Test
    public void doOptions() throws Exception {
        HttpUtils httpUtils = SimpleHttpUtils.createDefault();

        List<String> methods = httpUtils.doOptions("http://localhost:8888/simple/all");

        System.out.println(methods);
    }

    @Test
    public void doGetHttps() throws IOException {
        HttpRequested requested = new HttpRequested("https://127.0.0.1:8443");
        HttpRequestConfig requestConfig = new HttpRequestConfig();
        requestConfig.setIgnoreSSL(true);
        HttpResponsed responsed = SimpleHttpUtils.createDefault().doGet(requested, requestConfig);
        System.out.println(responsed.getStringBody("utf-8"));
    }


}