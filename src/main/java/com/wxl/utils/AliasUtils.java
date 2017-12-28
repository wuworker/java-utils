package com.wxl.utils;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by wuxingle on 2017/11/24.
 * 别名工具类
 */
public class AliasUtils {


    /**
     * 蛇形命名转驼峰
     */
    public static String snakeToCamel(String name) {
        if (!StringUtils.hasText(name)) {
            return name;
        }
        int index = 0;
        while ((index = name.indexOf("_")) != -1) {
            //最后一位
            if (index == name.length() - 1) {
                return name.substring(0, name.length() - 1);
            }
            char c = name.charAt(index + 1);
            name = name.substring(0, index)
                    + Character.toUpperCase(c)
                    + name.substring(index + 2, name.length());
        }
        return name;
    }

    /**
     * 驼峰命名转蛇形
     */
    public static String camelToSnake(String name) {
        if (!StringUtils.hasText(name)) {
            return name;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                name = name.substring(0, i)
                        + "_"
                        + Character.toLowerCase(c)
                        + name.substring(i + 1, name.length());
                i = i + 1;
            }
        }
        return name;
    }


    /**
     * 遍历json，转换别名
     */
    public static Object convertOfJson(Object json,Function<String,String> alias){
        if(json instanceof Map){
            Map map = (Map)json;
            return convertOfJsonMap(map,alias);
        }
        if(json instanceof List){
            List list = (List)json;
            return convertOfJsonList(list,alias);
        }
        throw new IllegalArgumentException("input must is a json(Map or List),but actual is "+ json.getClass().getName());
    }


    public static Map<String,Object> convertOfJsonMap(Map<String,Object> json, Function<String,String> alias){
        Map<String,Object> map = new HashMap<>(json.size());
        for(Map.Entry<String,Object> entry:json.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();
            String name = alias.apply(key);
            if(name == null){
                name = key;
            }
            if(value instanceof Map){
                Map<String,Object> valueMap = (Map<String,Object>)value;
                value = convertOfJsonMap(valueMap,alias);
            }
            if(value instanceof List){
                List<Object> valueList = (List<Object>)value;
                value = convertOfJsonList(valueList,alias);
            }

            map.put(name,value);
        }
        return map;
    }


    public static List<Object> convertOfJsonList(List<Object> json,Function<String,String> alias){
        List<Object> list = new ArrayList<>(json.size());
        for(Object obj:json){
            if(obj instanceof Map){
                list.add(convertOfJsonMap((Map<String,Object>)obj,alias));
            } else {
                list.add(obj);
            }
        }
        return list;
    }










}




