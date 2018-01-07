package com.wxl.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by wuxingle on 2018/1/5.
 * json相关工具类
 */
public class JsonUtils {

    /**
     * 格式化输出,必须为json对象
     */
    public static String toPrettyFormat(Object json) {
        return JSON.toJSONString(json, true);
    }

    /**
     * 往json格式的数据放入值
     */
    public static Object put(Object json, String key, Object value) {
        return putVal(json, key, value, null, false);
    }

    public static Object put(Object json, String key, Object value, String split) {
        return putVal(json, key, value, split, false);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> put(Map<String, Object> json, String key, Object value) {
        return (Map<String, Object>) putVal(json, key, value, null, false);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> put(Map<String, Object> json, String key, Object value, String split) {
        return (Map<String, Object>) putVal(json, key, value, split, false);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> put(List<Object> json, String key, Object value) {
        return (List<Object>) putVal(json, key, value, null, false);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> put(List<Object> json, String key, Object value, String split) {
        return (List<Object>) putVal(json, key, value, split, false);
    }

    /**
     * 只有原来没有值时才放入
     */
    public static Object putIfAbsent(Object json, String key, Object value) {
        return putVal(json, key, value, null, true);
    }

    public static Object putIfAbsent(Object json, String key, Object value, String split) {
        return putVal(json, key, value, split, true);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> putIfAbsent(Map<String, Object> json, String key, Object value) {
        return (Map<String, Object>) putVal(json, key, value, null, true);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> putIfAbsent(Map<String, Object> json, String key, Object value, String split) {
        return (Map<String, Object>) putVal(json, key, value, split, true);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> putIfAbsent(List<Object> json, String key, Object value) {
        return (List<Object>) putVal(json, key, value, null, true);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> putIfAbsent(List<Object> json, String key, Object value, String split) {
        return (List<Object>) putVal(json, key, value, split, true);
    }

    /**
     * 在对list操作时,用数字表示索引
     * <pre> {@code
     *
     *
     *
     *
     *
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    private static Object putVal(Object json, String key, Object value, String split, boolean onlyIfAbsent) {
        Assert.notNull(json, "json can not null");
        Assert.hasText(key, "put key can not empty");
        //默认用.分隔
        if (StringUtils.isEmpty(split)) {
            split = "\\.";
        }
        String[] keys = key.split(split);
        Object findJson = json;
        int i = 0;
        //循环找到最后一层key的值
        for (int len = keys.length - 1; i < len; i++) {
            Object v;
            String currentKey = keys[i];
            String nextKey = keys[i + 1];
            if (findJson instanceof Map) {
                Map<String, Object> findMap = (Map<String, Object>) findJson;
                v = findMap.get(currentKey);
                if (v != null) {
                    findJson = v;
                }
                //下一个为数字则新建list否则新建map
                else if (nextKey.matches("^\\d+$")) {
                    List<Object> list = new ArrayList<>();
                    findMap.put(currentKey, list);
                    findJson = list;
                } else {
                    Map<String, Object> map = new HashMap<>();
                    findMap.put(currentKey, map);
                    findJson = map;
                }
            } else if (findJson instanceof List) {
                List<Object> list = (List<Object>) findJson;
                int index = Integer.parseInt(currentKey);
                boolean toLarge;
                if (toLarge = (index >= list.size())) {
                    for (int j = list.size(); j < index; j++) {
                        list.add(null);
                    }
                    v = null;
                } else {
                    v = list.get(index);
                }
                if (v != null) {
                    findJson = v;
                } else if (nextKey.matches("^\\d+$")) {
                    List<Object> subList = new ArrayList<>();
                    if (toLarge) {
                        list.add(subList);
                    } else {
                        list.set(index, subList);
                    }
                    findJson = subList;
                } else {
                    Map<String, Object> map = new HashMap<>();
                    if (toLarge) {
                        list.add(map);
                    } else {
                        list.set(index, map);
                    }
                    findJson = map;
                }
            } else {
                if (i == 0) {
                    throw new IllegalArgumentException("input object is not a json,must is List or Map, actual is :" + json);
                }
                throw new IllegalArgumentException("can not put '" + key + "', "
                        + "because get '" + keys[i - 1] + "' value is '" + findJson + "'");
            }
        }
        if (findJson instanceof Map) {
            Map<String, Object> findMap = (Map<String, Object>) findJson;
            Object v = findMap.get(keys[i]);
            if (v == null || !onlyIfAbsent) {
                findMap.put(keys[i], value);
            }
        } else if (findJson instanceof List) {
            List<Object> findList = (List<Object>) findJson;
            //如果最后一个是list类型,那么key必须是数字索引
            int index = Integer.parseInt(keys[i]);
            if (index >= findList.size()) {
                for (int j = findList.size(); j < index; j++) {
                    findList.add(null);
                }
                findList.add(value);
            } else {
                Object old = findList.get(index);
                if (old == null || !onlyIfAbsent) {
                    findList.set(index, value);
                }
            }
        } else {
            if (i == 0) {
                throw new IllegalArgumentException("input object is not a json,must is List or Map, actual is :" + json);
            }
            throw new IllegalArgumentException("can not put '" + key + "', "
                    + "because get '" + keys[i - 1] + "' value is '" + findJson + "'");
        }
        return json;
    }


    /**
     * 从json中取出数据
     */
    public static <T> T get(Object json, String key, Class<T> clazz) {
        return getVal(json, key, clazz, null);
    }

    public static <T> T get(Object json, String key, Class<T> clazz, String split) {
        return getVal(json, key, clazz, split);
    }

    public static Object get(Object json, String key) {
        return getVal(json, key, Object.class, null);
    }

    public static Object get(Object json, String key, String split) {
        return getVal(json, key, Object.class, split);
    }

    public static String getString(Object json, String key) {
        return getString(json, key, null);
    }

    public static String getString(Object json, String key, String split) {
        Object obj = getVal(json, key, Object.class, split);
        return obj == null ? null : obj.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T> T getVal(Object json, String key, Class<T> clazz, String split) {
        Assert.notNull(json, "json map can not null");
        Assert.hasText(key, "put key can not empty");
        //默认用.分隔
        if (StringUtils.isEmpty(split)) {
            split = "\\.";
        }
        String[] keys = key.split(split);
        Object findJson = json;
        Object val = null;
        for (int i = 0; i < keys.length; i++) {
            if (findJson instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) findJson;
                val = map.get(keys[i]);
            } else if (findJson instanceof List) {
                List<Object> list = (List<Object>) findJson;
                val = list.get(Integer.parseInt(keys[i]));
            } else {
                if (i == 0) {
                    throw new IllegalArgumentException("input object is not a json,must is List or Map, actual is :" + json);
                }
                throw new IllegalArgumentException("can not get '" + key + "', because get '"
                        + keys[i - 1] + "' value is not map or list, value=" + findJson);
            }
            if (val == null) {
                break;
            }
            findJson = val;
        }
        return (T) val;
    }


}


