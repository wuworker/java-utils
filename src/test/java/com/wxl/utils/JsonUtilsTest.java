package com.wxl.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuxingle on 2018/1/5.
 * jsonTest
 */
public class JsonUtilsTest {

    @Test
    public void testPut(){
        Map<String,Object> json = new HashMap<>();

        json = JsonUtils.put(json,"user.info","nice");
        System.out.println(JsonUtils.toPrettyFormat(json));

        json = JsonUtils.put(json,"user.grade.0",99);
        System.out.println(JsonUtils.toPrettyFormat(json));

        json = JsonUtils.put(json,"user.grade.3",100);
        System.out.println(JsonUtils.toPrettyFormat(json));

        json = JsonUtils.put(json,"user.grade.2.a","aa");
        System.out.println(JsonUtils.toPrettyFormat(json));

        json = JsonUtils.put(json,"0","aa");
        System.out.println(JsonUtils.toPrettyFormat(json));

        json = JsonUtils.putIfAbsent(json,"user","bb");
        System.out.println(JsonUtils.toPrettyFormat(json));

        json = JsonUtils.put(json,"user","bb");
        System.out.println(JsonUtils.toPrettyFormat(json));
    }


    @Test
    public void testGet(){
        Map<String,Object> json = new HashMap<>();
        json = JsonUtils.put(json,"user.info","nice");
        json = JsonUtils.put(json,"user.grade.0",99);
        json = JsonUtils.put(json,"user.grade.3",100);
        json = JsonUtils.put(json,"user.grade.2.a","aaa");
        json = JsonUtils.put(json,"0","aa");
        System.out.println(JsonUtils.toPrettyFormat(json));

        String aa = JsonUtils.getString(json,"0");
        System.out.println(aa);

        String aaa = JsonUtils.getString(json,"user.grade.2.a");
        System.out.println(aaa);

        try {
            Object nulll = JsonUtils.get(json,"user.grade.0.key");
        }catch (Exception e){
            e.printStackTrace();
        }

        List list = JsonUtils.get(json,"user.grade",List.class);
        System.out.println(list);
    }





}