package com.wxl.utils;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by wuxingle on 2017/12/17 0017.
 * 集合相关工具类
 */
public class ListMapUtils {

    /**
     * 对list分页
     *
     * @param pageNo   第几页
     * @param pageSize 一页几个
     */
    public static <T> List<T> pageOfList(List<T> list, int pageNo, int pageSize) {
        Assert.notNull(list, "list can not null");
        Assert.isTrue(pageNo > 0 && pageSize > 0, "pageNo and pageSize must > 0");
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        int start = (pageNo - 1) * pageSize;
        if (start >= list.size()) {
            return new ArrayList<>();
        }
        int end = start + pageSize;
        return list.subList(start, end > list.size() ? list.size() : end);
    }


    /**
     * 获取map里的值
     */
    public static <K> String getMapStringValue(Map<K, Object> map, K key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

    @SuppressWarnings("unchecked")
    public static <K, V> V getMapValue(Map<K, Object> map, K key, Class<V> clazz) {
        return (V) map.get(key);
    }


    /**
     * 把map的value转为string
     */
    public static <K, V> Map<K, String> toStringMap(Map<K, V> map) {
        Map<K, String> result = new HashMap<>(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            result.put(key, value == null ? null : value.toString());
        }
        return result;
    }


    /**
     * 根据value拿key，拿第一个
     */
    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 合并2个map,转为hashMap
     */
    public static <K, V> Map<K, V> mergeToHash(Map<K, V> map1, Map<K, V> map2) {
        Map<K, V> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(map1)) {
            map.putAll(map1);
        }
        if (!CollectionUtils.isEmpty(map2)) {
            map.putAll(map2);
        }
        return map;
    }


}

