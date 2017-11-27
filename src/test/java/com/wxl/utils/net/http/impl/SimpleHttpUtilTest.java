package com.wxl.utils.net.http.impl;

import com.wxl.utils.net.http.HttpRequested;
import com.wxl.utils.net.http.HttpResponsed;
import com.wxl.utils.net.http.HttpUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuxingle on 2017/7/14 0014.
 *
 */
public class SimpleHttpUtilTest {


    @Test
    public void doGet() throws Exception {
        HttpUtil httpUtil = SimpleHttpUtil.custom()
                .setConnectTimeout(10000)
                .setReadTimeout(10000)
                .build();
        Map<String,String> params = new HashMap<>();
        params.put("name","哈哈");
        params.put("age","23");

        //byte[] res = httpUtil.doGet("http://localhost:8888/simple/all",params);

        HttpRequested requested = new HttpRequested("http://localhost:8888/simple/all");
        requested.addParam("name","哈哈");
        requested.addParam("age","23");
        requested.setConTimeout(3000);
        requested.setReadTimeout(3000);
        HttpResponsed responsed = httpUtil.doGet(requested,false);

        System.out.println(responsed.getStringBody("utf-8"));
    }

    @Test
    public void doHead()throws Exception{
        HttpUtil httpUtil = SimpleHttpUtil.custom()
                .setConnectTimeout(2000)
                .setReadTimeout(2000)
                .build();

        Map<String,String> heads = httpUtil.doHead("http://localhost:8888/simple/all");

        System.out.println(heads);
    }


    @Test
    public void doPOST()throws Exception{
        HttpUtil httpUtil = SimpleHttpUtil.createDefault();

        String body = "{\"name\":\"hahahha\",\"age\":303}";

        Map<String,String> heads = new HashMap<>();
        heads.put("Content-Type","application/json");

        byte[] res = httpUtil.doPost("http://localhost:8888/simple/json",body.getBytes(),heads);

        System.out.println(new String(res));
    }


    @Test
    public void doPUT()throws Exception{
        HttpUtil httpUtil = SimpleHttpUtil.createDefault();

        String body = "{\"name\":\"hahahha\",\"age\":303}";

        Map<String,String> heads = new HashMap<>();
        heads.put("Content-Type","application/json");

        byte[] res = httpUtil.doPut("http://localhost:8888/simple/json",body.getBytes(),heads);

        System.out.println(new String(res));
    }

    @Test
    public void doPATCH()throws Exception{
        HttpUtil httpUtil = SimpleHttpUtil.createDefault();

        String body = "{\"name\":\"hahahha\",\"age\":303}";

        Map<String,String> heads = new HashMap<>();
        heads.put("Content-Type","application/json");

        byte[] res = httpUtil.doPatch("http://localhost:8888/simple/json",body.getBytes(),heads);

        System.out.println(new String(res));
    }

    @Test
    public void doDelete()throws Exception{
        HttpUtil httpUtil = SimpleHttpUtil.createDefault();

        byte[] res  = httpUtil.doDelete("http://localhost:8888/simple/all");

        System.out.println(new String(res));
    }


    @Test
    public void doOptions()throws Exception{
        HttpUtil httpUtil = SimpleHttpUtil.createDefault();

        List<String> methods = httpUtil.doOptions("http://localhost:8888/simple/all");

        System.out.println(methods);
    }


}