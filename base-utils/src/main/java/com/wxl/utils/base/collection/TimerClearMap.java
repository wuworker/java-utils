package com.wxl.utils.base.collection;

import com.wxl.utils.base.annotation.ThreadSafe;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by wuxingle on 2017/12/30 0030.
 * 定时清除所有数据的map
 * 单位为秒
 */
@ThreadSafe
public class TimerClearMap<K, V> extends AbstractMap<K, V>
        implements ConcurrentMap<K,V> {

    //默认缓存10分钟
    private static final int DEFAULT_CLEAR_TIME = 10 * 60;

    private static final int DEFAULT_CAPACITY = 1 << 4;

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private static Timer timer = new Timer(true);

    private TimerTask clearTask;

    private ConcurrentMap<K, V> concurrentMap;


    public TimerClearMap() {
        this(DEFAULT_CLEAR_TIME);
    }

    public TimerClearMap(int time) {
        this(time, DEFAULT_CAPACITY);
    }

    public TimerClearMap(int time, int initCap) {
        this(time, initCap, DEFAULT_LOAD_FACTOR);
    }

    public TimerClearMap(int time, int initCap, float loadFactor) {
        Assert.isTrue(time > 0, "time must > 0");
        time = time * 1000;
        concurrentMap = new ConcurrentHashMap<>(initCap, loadFactor);
        clearTask = new ClearTask<>(concurrentMap);
        timer.scheduleAtFixedRate(clearTask, time, time);
    }

    public TimerClearMap(Map<? extends K, ? extends V> map) {
        this(DEFAULT_CLEAR_TIME, map);
    }

    public TimerClearMap(int time, Map<? extends K, ? extends V> map) {
        Assert.isTrue(time > 0, "time must > 0");
        time = time * 1000;
        concurrentMap = new ConcurrentHashMap<>(map);
        clearTask = new ClearTask<>(concurrentMap);
        timer.scheduleAtFixedRate(clearTask, time, time);
    }

    /**
     * 取消定时任务
     */
    public void cancelClear(){
        clearTask.cancel();
    }

    @Override
    public int size() {
        return concurrentMap.size();
    }

    @Override
    public boolean isEmpty() {
        return concurrentMap.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        return concurrentMap.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return concurrentMap.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return concurrentMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        return concurrentMap.put(key,value);
    }

    @Override
    public V remove(Object key) {
        return concurrentMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        concurrentMap.putAll(m);
    }

    @Override
    public void clear() {
        concurrentMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return concurrentMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return concurrentMap.values();
    }

    @Override
    public boolean equals(Object o) {
        return concurrentMap.equals(o);
    }

    @Override
    public int hashCode() {
        return concurrentMap.hashCode();
    }

    @Override
    public String toString() {
        return concurrentMap.toString();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return concurrentMap.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return concurrentMap.getOrDefault(key,defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        concurrentMap.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        concurrentMap.replaceAll(function);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return concurrentMap.computeIfAbsent(key,mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return concurrentMap.computeIfPresent(key,remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return concurrentMap.compute(key,remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return concurrentMap.merge(key,value,remappingFunction);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return concurrentMap.remove(key,value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return concurrentMap.replace(key,oldValue,newValue);
    }

    @Override
    public V putIfAbsent(K key, V value){
        return concurrentMap.putIfAbsent(key,value);
    }

    @Override
    public V replace(K key, V value){
        return concurrentMap.replace(key,value);
    }

    /**
     * 定时清除任务
     */
    private static class ClearTask<K, V> extends TimerTask {

        private Map<K, V> map;

        public ClearTask(Map<K, V> map) {
            this.map = map;
        }

        @Override
        public void run() {
            map.clear();
        }
    }

}



