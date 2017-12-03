package com.wxl.utils.map.impl;

import com.wxl.utils.annotation.UnThreadSafe;
import com.wxl.utils.map.CacheMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * Created by wuxingle on 2017/12/1.
 * 延迟删除缓存map.
 * 当从map获取数据时，才清除过期数据.
 * 注意:
 * 在对map迭代时，当hasNext返回true时，next()拿到发现是过期数据，则自动获取下一个.
 * 如果最后一个仍是过期,则以key和value都为null的对象代替.
 * 所以在迭代时，可能会拿到的key和value都是空的数据.
 */
@UnThreadSafe
public class LazyCacheMap<K, V> extends AbstractMap<K, V>
        implements CacheMap<K, V>, Serializable, Cloneable {

    private static final long serialVersionUID = 4916775860465620257L;

    /**
     * 已过期的entry
     * 当进行迭代时,hasNext返回true,调用next()后获取的是过期数据，而且这是最后一个元素.
     * 出现这种情况时,以这个对象进行替代.
     */
    private static final Entry expireEntry = new SimpleImmutableEntry<>(null, null);

    private HashMap<K, LazyCacheEntry<K, V>> cacheMap;

    private transient Set<Entry<K, V>> entrySet;

    public LazyCacheMap() {
        cacheMap = new HashMap<>();
    }

    public LazyCacheMap(int initialCapacity) {
        cacheMap = new HashMap<>(initialCapacity);
    }

    public LazyCacheMap(int initialCapacity, float loadFactor) {
        cacheMap = new HashMap<>(initialCapacity, loadFactor);
    }

    /**
     * 设置Key的过期时间
     *
     * @param expire 毫秒
     * @return key是否存在
     */
    @Override
    public boolean setExpire(K key, long expire) {
        LazyCacheEntry<K, V> entry = getAndRemoveIfExpire(key);
        if (entry == null) {
            return false;
        }
        if (isExpireTime(expire)) {
            remove(key);
        } else {
            entry.setExpire(expire);
        }
        return true;
    }

    /**
     * 设置Key为持久化
     *
     * @return key是否存在
     */
    @Override
    public boolean setPersistent(K key) {
        return setExpire(key, PERSISTENT_KEY);
    }

    /**
     * 获取剩余存活时间
     */
    @Override
    public long ttl(K key) {
        LazyCacheEntry<K, V> entry = getAndRemoveIfExpire(key);
        return entry == null ? NOT_EXIST_KEY : entry.ttl();
    }

    /**
     * 是否是持久化的key
     */
    @Override
    public boolean isPersistent(K key) {
        return ttl(key) == PERSISTENT_KEY;
    }

    /**
     * 放入key并设置过期时间
     */
    @Override
    public V put(K key, V value, long expire) {
        if (isExpireTime(expire)) {
            return remove(key);
        }
        LazyCacheEntry<K, V> entry = new LazyCacheEntry<>(key, value, expire);
        entry = cacheMap.put(key, entry);
        return entry == null ? null : entry.getValue();
    }


    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    @Override
    public boolean containsValue(Object value) {
        Iterator<Entry<K, LazyCacheEntry<K, V>>> it = cacheMap.entrySet().iterator();
        while (it.hasNext()) {
            LazyCacheEntry<K, V> entry = it.next().getValue();
            if (entry.isExpire()) {
                it.remove();
            } else if (Objects.equals(value, entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        LazyCacheEntry<K, V> cacheEntry = getAndRemoveIfExpire(key);
        return cacheEntry != null;
    }

    @Override
    public V get(Object key) {
        LazyCacheEntry<K, V> entry = getAndRemoveIfExpire(key);
        return entry == null ? null : entry.getValue();
    }


    @Override
    public V remove(Object key) {
        LazyCacheEntry<K, V> value = cacheMap.remove(key);
        if (value == null) {
            return null;
        }
        return value.isExpire() ? null : value.getValue();
    }

    @Override
    public V put(K key, V value) {
        return put(key, value, PERSISTENT_KEY);
    }

    @Override
    public void clear() {
        cacheMap.clear();
    }

    @Override
    public String toString() {
        Iterator<Entry<K, LazyCacheEntry<K, V>>> it = cacheMap.entrySet().iterator();
        StringBuilder sb = new StringBuilder("{");
        while (it.hasNext()) {
            LazyCacheEntry<K, V> entry = it.next().getValue();
            if (entry.isExpire()) {
                it.remove();
                continue;
            }
            sb.append(entry).append(", ");
        }
        return sb.toString().endsWith("{") ?
                "{}"
                : sb.substring(0, sb.length() - 2) + "}";
    }

    /**
     * 清理过期数据
     */
    public int clearExpire() {
        int count = 0;
        Iterator<Entry<K, LazyCacheEntry<K, V>>> it = cacheMap.entrySet().iterator();
        while (it.hasNext()) {
            LazyCacheEntry<K, V> entry = it.next().getValue();
            if (entry.isExpire()) {
                count++;
                it.remove();
            }
        }
        return count;
    }

    /**
     * 序列化
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        clearExpire();
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        clearExpire();
    }

    /**
     * cloneable
     */
    @SuppressWarnings("unchecked")
    @Override
    public LazyCacheMap<K, V> clone() {
        clearExpire();
        LazyCacheMap<K, V> result;
        try {
            result = (LazyCacheMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
        result.cacheMap = (HashMap<K, LazyCacheEntry<K, V>>) cacheMap.clone();
        result.entrySet = null;
        return result;
    }


    /**
     * 获取cacheEntry，如果过期则删除
     */
    private LazyCacheEntry<K, V> getAndRemoveIfExpire(Object key) {
        LazyCacheEntry<K, V> entry = cacheMap.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpire()) {
            cacheMap.remove(key);
            return null;
        }
        return entry;
    }

    /**
     * 是否是过期的时间
     */
    private boolean isExpireTime(long expire) {
        return expire <= 0 && expire != PERSISTENT_KEY;
    }

    public static class LazyCacheEntry<K, V> extends SimpleEntry<K, V> {
        private static final long serialVersionUID = -8977888694870171813L;

        private Long expire;

        public LazyCacheEntry(K key, V value, long expire) {
            super(key, value);
            setExpire(expire);
        }

        public long ttl() {
            if (expire == null) {
                return PERSISTENT_KEY;
            }
            return expire - System.currentTimeMillis();
        }

        public boolean isExpire() {
            return expire!=null && ttl() <= 0;
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
        public String toString() {
            return expire == null ?
                    getKey() + "=" + getValue()+"(Persistent)"
                    : getKey() + "=" + getValue() + "(" + ttl() + ")";
        }
    }

    final class EntrySet extends AbstractSet<Entry<K, V>> {

        @Override
        public int size() {
            clearExpire();
            return cacheMap.size();
        }

        @Override
        public void clear() {
            LazyCacheMap.this.clear();
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new CacheEntryIterator();
        }

        @Override
        public boolean contains(Object o) {
            if (o instanceof Entry) {
                Entry<?, ?> entry = (Entry<?, ?>) o;
                LazyCacheEntry<K, V> cacheEntry = getAndRemoveIfExpire(entry.getKey());
                return cacheEntry != null
                        && Objects.equals(cacheEntry.getValue(), entry.getValue());
            }
            return false;
        }

        @Override
        public boolean remove(Object o) {
            if (o instanceof Entry) {
                Entry<?, ?> entry = (Entry<?, ?>) o;
                return LazyCacheMap.this.remove(entry.getKey(), entry.getValue());
            }
            return false;
        }

        @Override
        public String toString() {
            return LazyCacheMap.this.toString();
        }
    }

    /**
     * 迭代器，迭代K,V
     */
    class CacheEntryIterator implements Iterator<Entry<K, V>> {

        private Iterator<K> cacheKeyIt;

        private ListIterator<LazyCacheEntry<K,V>> cacheEntryIt;

        CacheEntryIterator() {
            cacheKeyIt = cacheMap.keySet().iterator();
            cacheEntryIt = new ArrayList<>(cacheMap.values()).listIterator();
        }

        @Override
        public void remove() {
            cacheKeyIt.remove();
            cacheEntryIt.remove();
        }

        @Override
        public boolean hasNext() {
            if(cacheEntryIt.hasNext()){
                LazyCacheEntry<K,V> entry = cacheEntryIt.next();
                if(entry.isExpire()){
                    cacheKeyIt.next();
                    remove();
                    return hasNext();
                }
                cacheEntryIt.previous();
                return true;
            }
            return false;
        }

        @Override
        public Entry<K, V> next() {
            LazyCacheEntry<K, V> entry = cacheEntryIt.next();
            cacheKeyIt.next();
            //如果已过期则取下一个，没有下一个则返回空对象
            if (entry.isExpire()) {
                remove();
                return hasNext() ? next() : expireEntry;
            }
            return entry;
        }
    }


}
