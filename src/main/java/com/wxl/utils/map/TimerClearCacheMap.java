package com.wxl.utils.map;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Set;
import java.util.Timer;

/**
 * Created by wuxingle on 2017/12/17 0017.
 * 定时清理缓存map()
 */
public class TimerClearCacheMap<K, V> extends AbstractMap<K, V>
        implements CacheMap<K, V>, Serializable {

    private static final long serialVersionUID = 2217240140781148495L;

    private static final Timer TIMER = new Timer(true);


    @Override
    public boolean setExpire(K key, long expire) {
        return false;
    }

    @Override
    public boolean setPersistent(K key) {
        return false;
    }

    @Override
    public long ttl(K key) {
        return 0;
    }

    @Override
    public boolean isPersistent(K key) {
        return false;
    }

    @Override
    public V put(K key, V value, long expire) {
        return null;
    }

    @Override
    public V putIfAbsent(K key, V value, long expire) {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }
}






