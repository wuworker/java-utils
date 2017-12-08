package com.wxl.utils.map.impl;

import com.wxl.utils.annotation.ThreadSafe;
import com.wxl.utils.map.CacheMap;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by wuxingle on 2017/12/3 0003.
 * 同步的缓存map.
 * 默认使用LazyCacheMap.
 * 因为用的锁不是对象本身,所以迭代等复合操作时,
 * 需要用getLock()方法获取锁.
 */
@ThreadSafe
public class SynchronizedCacheMap<K, V> extends AbstractMap<K, V>
        implements CacheMap<K, V>, Serializable {

    private static final long serialVersionUID = -5012066891477357005L;

    private CacheMap<K, V> cacheMap;

    private final Map<K, V> lockProxyMap;

    public SynchronizedCacheMap() {
        cacheMap = new LazyCacheMap<>();
        lockProxyMap = Collections.synchronizedMap(cacheMap);
    }

    public SynchronizedCacheMap(int initialCapacity) {
        cacheMap = new LazyCacheMap<>(initialCapacity);
        lockProxyMap = Collections.synchronizedMap(cacheMap);
    }

    public SynchronizedCacheMap(int initialCapacity, float loadFactor) {
        cacheMap = new LazyCacheMap<>(initialCapacity, loadFactor);
        lockProxyMap = Collections.synchronizedMap(cacheMap);
    }

    public SynchronizedCacheMap(CacheMap<K, V> cacheMap){
        Assert.notNull(cacheMap,"cacheMap can not null");
        this.cacheMap = cacheMap;
        lockProxyMap = Collections.synchronizedMap(this.cacheMap);
    }


    /**
     * 获取锁
     */
    public Object getSynchronizedLock(){
        return lockProxyMap;
    }


    //---implements cacheMap

    @Override
    public boolean setExpire(K key, long expire) {
        synchronized (lockProxyMap) {
            return cacheMap.setExpire(key, expire);
        }
    }

    @Override
    public boolean setPersistent(K key) {
        synchronized (lockProxyMap) {
            return cacheMap.setPersistent(key);
        }
    }

    @Override
    public long ttl(K key) {
        synchronized (lockProxyMap) {
            return cacheMap.ttl(key);
        }
    }

    @Override
    public boolean isPersistent(K key) {
        synchronized (lockProxyMap) {
            return cacheMap.isPersistent(key);
        }
    }

    @Override
    public V put(K key, V value, long expire) {
        synchronized (lockProxyMap) {
            return cacheMap.put(key, value, expire);
        }
    }

    @Override
    public V putIfAbsent(K key, V value, long expire) {
        synchronized (lockProxyMap) {
            return cacheMap.putIfAbsent(key, value, expire);
        }
    }

    // implements  map

    @Override
    public int size() {
        return lockProxyMap.size();
    }

    @Override
    public boolean isEmpty() {
        return lockProxyMap.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        return lockProxyMap.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return lockProxyMap.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return lockProxyMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        return lockProxyMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return lockProxyMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        lockProxyMap.putAll(m);
    }

    @Override
    public void clear() {
        lockProxyMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return lockProxyMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return lockProxyMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return lockProxyMap.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || lockProxyMap.equals(o);
    }

    @Override
    public int hashCode() {
        return lockProxyMap.hashCode();
    }

    @Override
    public String toString() {
        return lockProxyMap.toString();
    }

    /**
     * 序列化会保持引用关系
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        synchronized (lockProxyMap){
            out.defaultWriteObject();
        }
    }


    //implements jdk8

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return lockProxyMap.getOrDefault(key,defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        lockProxyMap.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        lockProxyMap.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return lockProxyMap.putIfAbsent(key,value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return lockProxyMap.remove(key,value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return lockProxyMap.replace(key,oldValue,newValue);
    }

    @Override
    public V replace(K key, V value) {
        return lockProxyMap.replace(key,value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return lockProxyMap.computeIfAbsent(key,mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return lockProxyMap.computeIfPresent(key,remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return lockProxyMap.compute(key,remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return lockProxyMap.merge(key,value,remappingFunction);
    }
}
