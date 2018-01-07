package com.wxl.utils;

import org.junit.Test;

import java.util.ArrayList;
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

        json = JsonUtils.putIfAbsent(json,"user.grade.1","1111");
        System.out.println(JsonUtils.toPrettyFormat(json));

        json = JsonUtils.putIfAbsent(json,"user.grade.1","22222");
        System.out.println(JsonUtils.toPrettyFormat(json));

        json = JsonUtils.put(json,"0","aa");
        System.out.println(JsonUtils.toPrettyFormat(json));

        json = JsonUtils.putIfAbsent(json,"user","bb");
        System.out.println(JsonUtils.toPrettyFormat(json));

        json = JsonUtils.put(json,"user","bb");
        System.out.println(JsonUtils.toPrettyFormat(json));

        List<Object> list = new ArrayList<>();
        list.add(1);
        list.add("abc");

        list = JsonUtils.put(list,"3.test",json);
        System.out.println(JsonUtils.toPrettyFormat(list));

        list = JsonUtils.putIfAbsent(list,"1","no");
        System.out.println(JsonUtils.toPrettyFormat(list));

        list = JsonUtils.put(list,"1","yes");
        System.out.println(JsonUtils.toPrettyFormat(list));
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

        List list2 = JsonUtils.get(json,"nice.age",List.class);
        System.out.println(list2);
    }





}