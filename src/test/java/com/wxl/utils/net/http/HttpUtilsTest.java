package com.wxl.utils.net.http;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Create by wuxingle on 2018/5/1
 */
public class HttpUtilsTest {

    @Test
    public void testQuery() {
        Map<String, String> map = new HashMap<>();
        map.put("name", "wxl");
        map.put("redirect", "https://www.baidu.com");
        map.put("age", "123");
        map.put("a", null);

        String s = HttpUtils.toQuery(map, "utf-8");
        System.out.println(s);

        System.out.println(HttpUtils.fromQuery(s, "utf-8"));
    }

    @Test
    public void testBuildURL(){
        Map<String, String> map = new HashMap<>();
        map.put("name", "wxl");
        map.put("redirect", "https://www.baidu.com");
        map.put("age", "123");
        map.put("kw","gg123");
        String url = HttpUtils.buildURL("http://www.baidu.com?kw=gg#miao",map,"utf-8");

        System.out.println(url);
    }


    @Test
    public void test1() throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
        String encode = URLEncoder.encode("https://127.0.0.1:9999/name=wu&age=123", "utf-8");
        System.out.println(encode);

        URL url = new URL("https://127.0.0.1:9999/nice/hello?token=abc&redirect=" + encode + "#miao");
        System.out.println(url);
        System.out.println(url.getProtocol());
        System.out.println(url.getHost());
        System.out.println(url.getPort());
        System.out.println(url.getDefaultPort());
        System.out.println(url.getPath());
        System.out.println(url.getQuery());
        System.out.println(url.getAuthority());
        System.out.println(url.getRef());
        System.out.println(url.getUserInfo());
        System.out.println(url.getFile());

        System.out.println("--------------------");
        URI uri = new URI("https://127.0.0.1:9999/nice/hello?token=abc&redirect=" + encode + "#miao");
        System.out.println(uri);
        System.out.println(uri.getScheme());
        System.out.println(uri.getHost());
        System.out.println(uri.getPort());
        System.out.println(uri.getPath());
        System.out.println(uri.getQuery());
        System.out.println(uri.getAuthority());
        System.out.println(uri.getFragment());
        System.out.println(uri.getUserInfo());
        System.out.println("------raw------------");
        System.out.println(uri.getRawAuthority());
        System.out.println(uri.getRawFragment());
        System.out.println(uri.getRawPath());
        System.out.println(uri.getRawQuery());
        System.out.println(uri.getRawSchemeSpecificPart());
        System.out.println(uri.getRawUserInfo());

    }

}