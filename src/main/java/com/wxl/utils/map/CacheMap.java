package com.wxl.utils.map;

import java.util.Map;

/**
 * Created by wuxingle on 2017/12/1.
 * 缓存map
 */
public interface CacheMap<K,V> extends Map<K,V>{

    //持久的key
    long PERSISTENT_KEY = -1;

    //key不存在
    long NOT_EXIST_KEY = -2;

    /**
     * 设置Key的过期时间
     * @param expire 毫秒
     * @return key是否存在
     */
    boolean setExpire(K key,long expire);


    /**
     * 设置Key为持久化
     * @return key是否存在
     */
    boolean setPersistent(K key);

    /**
     * 获取剩余存活时间
     * @return PERSISTENT_KEY  说明是永不过期的key
     *         NOT_EXIST_KEY   说明key不存在
     * 返回-1不一定是持久化的key,有可能正好过期-1秒
     */
    long ttl(K key);


    /**
     * 是否是持久化的key
     */
    boolean isPersistent(K key);

    /**
     * 放入key并设置过期时间
     */
    V put(K key, V value, long expire);


    /**
     * 如果value不存在则放入
     */
    default V putIfAbsent(K key, V value, long expire) {
        V v = get(key);
        if (v == null) {
            v = put(key, value,expire);
        }

        return v;
    }


    /**
     * entry
     */
    interface CacheEntry<K,V> extends Entry<K,V> {

        Long getExpire();

        void setExpire(Long expire);

        /**
         * 是否过期
         */
        boolean isExpire();
    }


}












