package com.wxl.utils.map.impl;

import com.wxl.utils.map.CacheMap;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by wuxingle on 2017/12/1.
 * 一旦过期立即删除
 */
public class ImmediateCacheMap<K, V> extends AbstractMap<K, V> implements CacheMap<K, V> {

    private Set<CacheEntry<K, V>> cacheEntries = new HashSet<>();

    private Set<Entry<K, V>> entrySet;

    @Override
    public Long getExpire(K key) {
        if(key == null){
            for(CacheEntry<K,V> entry:cacheEntries){
                if(entry.getKey() == null){
                    return entry.expire;
                }
            }
        } else {
            for(CacheEntry<K,V> entry:cacheEntries){
                if(key.equals(entry.getKey())){
                    return entry.expire;
                }
            }
        }
        return null;
    }


    @Override
    public Long getPTTL(K key) {
        if(key == null){
            for(CacheEntry<K,V> entry:cacheEntries){
                if(entry.getKey() == null){
                    return entry.getPTTL();
                }
            }
        } else {
            for(CacheEntry<K,V> entry:cacheEntries){
                if(key.equals(entry.getKey())){
                    return entry.getPTTL();
                }
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value, long expire) {
        return null;
    }

    @Override
    public boolean isExpire(K key) {
        return false;
    }


    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }


    private CacheEntry<K, V> getByKey(K key){

    }

    static class CacheEntry<K, V> extends AbstractMap.SimpleEntry<K, V> implements Map.Entry<K, V> {
        Long expire;

        CacheEntry(K key, V value, long expire) {
            super(key, value);
            this.expire = expire;
        }

        @Override
        public V getValue() {
            if (isExpire()) {
                return null;
            }
            return super.getValue();
        }

        public Long getPTTL() {
            if (expire == null) {
                return null;
            }
            return expire - System.currentTimeMillis();
        }

        public boolean isExpire() {
            return getPTTL() != null && getPTTL() <= 0;
        }
    }


    final class EntrySet extends AbstractSet<Entry<K, V>> {

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new CacheEntryIterator();
        }

        @Override
        public int size() {
            cacheEntries.removeIf(CacheEntry::isExpire);
            return cacheEntries.size();
        }

    }


    final class CacheEntryIterator implements Iterator<Entry<K, V>> {

        private Iterator<CacheEntry<K, V>> cacheIt;

        CacheEntryIterator() {
            cacheIt = cacheEntries.iterator();
        }

        @Override
        public void remove() {
            cacheIt.remove();
        }

        @Override
        public void forEachRemaining(Consumer<? super Entry<K, V>> action) {

        }

        @Override
        public boolean hasNext() {
            return cacheIt.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            CacheEntry<K, V> cacheEntry = cacheIt.next();
            if (cacheEntry.isExpire()) {
                cacheIt.remove();
                return null;
            }
            return cacheEntry;
        }
    }


}
