package com.wxl.utils.net.http.impl;

import com.wxl.utils.net.http.HttpMethod;
import com.wxl.utils.net.http.HttpRequested;
import com.wxl.utils.net.http.HttpResponsed;
import com.wxl.utils.net.http.HttpUtils;
import org.junit.Test;

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
        requested.addParam("name","哈哈");
        requested.addParam("age","23");
        requested.setConTimeout(3000);
        requested.setReadTimeout(3000);
        HttpResponsed responsed = httpUtils.doGet(requested,false);

        System.out.println(responsed.getStringBody("utf-8"));
    }

    @Test
    public void doHead()throws Exception{
        HttpUtils httpUtils = HttpClientUtils.createDefault();

        Map<String,String> heads = httpUtils.doHead("http://localhost:8888/simple/all");

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

}