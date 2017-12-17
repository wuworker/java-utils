package com.wxl.utils;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wuxingle on 2017/12/17 0017.
 * 集合相关工具类
 */
public class ListMapUtils {

    /**
     * 对list分页
     * @param pageNo 第几页
     * @param pageSize 一页几个
     */
    public static <T> List<T> pageOfList(List<T> list, int pageNo, int pageSize) {
        Assert.notNull(list,"list can not null");
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


}

