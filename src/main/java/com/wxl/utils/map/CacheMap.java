package com.wxl.utils.map;

import java.util.Map;

/**
 * Created by wuxingle on 2017/12/1.
 * 缓存map
 */
public interface CacheMap<K,V> extends Map<K,V>{

    /**
     * 获取过期时间
     */
    Long getExpire(K key);

    /**
     * 获取剩余存活时间
     */
    Long getPTTL(K key);


    /**
     * 放入key并设置过期时间
     */
    V put(K key, V value, long expire);


    /**
     * 是否过期
     */
    boolean isExpire(K key);

}

