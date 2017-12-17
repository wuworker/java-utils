package com.wxl.utils.map;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wuxingle on 2017/12/17 0017.
 * 定时清理缓存map
 */
public class TimerClearCacheMap<K,V> extends AbstractCacheMap<K,V>
        implements Serializable {

    private static final long serialVersionUID = 2217240140781148495L;

    private static final Timer TIMER = new Timer(true);

    private Map<K,SyncCacheEntry<K,V>> cacheMap;

    public TimerClearCacheMap() {
        cacheMap = new ConcurrentHashMap<>();

    }

    @Override
    public boolean setExpire(K key, long expire) {
        if(isExpireTime(expire)){
            return remove(key) != null;
        }
        SyncCacheEntry<K, V> entry = cacheMap.get(key);
        if(entry == null){
            return false;
        }
        entry.setExpire(expire);
        return true;
    }

    @Override
    public boolean setPersistent(K key) {
        return setExpire(key, PERSISTENT_KEY);
    }

    @Override
    public long ttl(K key) {
        SyncCacheEntry<K, V> entry = cacheMap.get(key);
        return entry == null ? NOT_EXIST_KEY : entry.ttl();
    }

    @Override
    public boolean isPersistent(K key) {
        return ttl(key) == PERSISTENT_KEY;
    }

    @Override
    public V put(K key, V value, long expire) {
        if (isExpireTime(expire)) {
            return remove(key);
        }
        SyncCacheEntry<K, V> entry = new SyncCacheEntry<>(key, value, expire);
        entry = cacheMap.put(key, entry);
        return entry == null ? null : entry.getValue();
    }

    @Override
    public V putIfAbsent(K key, V value, long expire) {
        if (isExpireTime(expire)) {
            return remove(key);
        }
        SyncCacheEntry<K, V> entry = new SyncCacheEntry<>(key, value, expire);
        entry = cacheMap.putIfAbsent(key, entry);
        return entry == null ? null : entry.getValue();
    }

    @Override
    public int size() {
        return cacheMap.size();
    }

    @Override
    public boolean isEmpty() {
        return cacheMap.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        Collection<SyncCacheEntry<K, V>> values = cacheMap.values();
        for(SyncCacheEntry<K,V> entry:values){
            boolean eq = Objects.equals(entry.getValue(),value);
            if(eq){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return cacheMap.containsKey(key);
    }

    @Override
    public V get(Object key) {
        SyncCacheEntry<K, V> SyncCacheEntry = cacheMap.get(key);
        if(SyncCacheEntry == null){
            return null;
        }
        return SyncCacheEntry.getValue();
    }

    @Override
    public V put(K key, V value) {
        return put(key, value, PERSISTENT_KEY);
    }

    @Override
    public V remove(Object key) {
        SyncCacheEntry<K, V> entry = cacheMap.remove(key);
        return entry == null ? null : entry.getValue();
    }

    @Override
    public void clear() {
        cacheMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return cacheMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return super.values();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }



    private static class SyncCacheEntry<K,V> extends SimpleEntry<K,V> {

        private static final long serialVersionUID = -8421583925347309794L;
        private volatile Long expire;

        public SyncCacheEntry(K key, V value, long expire) {
            super(key, value);
            setExpire(expire);
        }

        public synchronized long ttl() {
            if (expire == null) {
                return PERSISTENT_KEY;
            }
            return expire - System.currentTimeMillis();
        }

        public synchronized boolean isExpire() {
            return expire != null && ttl() <= 0;
        }

        public Long getExpire() {
            return expire;
        }

        public void setExpire(long expire) {
            if (expire == PERSISTENT_KEY) {
                this.expire = null;
            } else {
                this.expire = expire + System.currentTimeMillis();
            }
        }

        @Override
        public synchronized String toString() {
            return expire == null ?
                       getKey() + "=" + getValue() + "(Persistent)"
                       : getKey() + "=" + getValue() + "(" + ttl() + ")";
        }
    }



}






