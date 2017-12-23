package com.wxl.utils.map;

import com.wxl.utils.annotation.UnThreadSafe;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by wuxingle on 2017/12/1.
 * 延迟删除缓存map.
 * 当从map获取数据时，才清除过期数据.
 * 在对map迭代时，当hasNext返回true时，next()拿到发现是过期数据，则自动获取下一个.
 * 如果最后一个仍是过期,则返回过期数据!
 */
@UnThreadSafe
public class LazyCacheMap<K, V> extends AbstractCacheMap<K, V>
        implements Serializable, Cloneable {

    private static final long serialVersionUID = 4916775860465620257L;

    private Map<K, CacheEntry<K, V>> cacheMap;

    private transient Set<Entry<K, V>> entrySet;

    private transient Set<K> keySet;

    private transient Collection<V> values;

    public LazyCacheMap() {
        cacheMap = new HashMap<>();
    }

    public LazyCacheMap(int initialCapacity) {
        cacheMap = new HashMap<>(initialCapacity);
    }

    public LazyCacheMap(int initialCapacity, float loadFactor) {
        cacheMap = new HashMap<>(initialCapacity, loadFactor);
    }

    public LazyCacheMap(Map<K, CacheEntry<K, V>> cacheMap) {
        Assert.notNull(cacheMap, "cacheMap can not null");
        this.cacheMap = cacheMap;
    }

    /**
     * 设置Key的过期时间
     *
     * @param expire 毫秒
     * @return key是否存在
     */
    @Override
    public boolean setExpire(K key, long expire) {
        CacheEntry<K, V> entry = getAndRemoveIfExpire(key);
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
        CacheEntry<K, V> entry = getAndRemoveIfExpire(key);
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
        CacheEntry<K, V> entry = new CacheEntry<>(key, value, expire);
        entry = cacheMap.put(key, entry);
        return entry == null ? null : entry.getValue();
    }

    @Override
    public int size() {
        clearExpire();
        return cacheMap.size();
    }

    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new KeySet();
        }
        return keySet;
    }

    @Override
    public Collection<V> values() {
        if (values == null) {
            values = new Values();
        }
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    @Override
    public boolean containsValue(final Object value) {
        Iterator<Entry<K, CacheEntry<K, V>>> it = cacheMap.entrySet().iterator();
        while (it.hasNext()) {
            CacheEntry<K, V> entry = it.next().getValue();
            if (entry.isExpire()) {
                it.remove();
                continue;
            }
            if (Objects.equals(value, entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        CacheEntry<K, V> cacheEntry = getAndRemoveIfExpire(key);
        return cacheEntry != null;
    }

    @Override
    public V get(Object key) {
        CacheEntry<K, V> entry = getAndRemoveIfExpire(key);
        return entry == null ? null : entry.getValue();
    }


    @Override
    public V remove(Object key) {
        CacheEntry<K, V> entry = removeEntry(key);
        return entry == null ? null : entry.getValue();
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
        Iterator<Entry<K, CacheEntry<K, V>>> it = cacheMap.entrySet().iterator();
        StringBuilder sb = new StringBuilder("{");
        while (it.hasNext()) {
            CacheEntry<K, V> entry = it.next().getValue();
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

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Iterator<Entry<K, CacheEntry<K, V>>> it = cacheMap.entrySet().iterator();
        while (it.hasNext()) {
            CacheEntry<K, V> entry = it.next().getValue();
            if (entry.isExpire()) {
                it.remove();
                continue;
            }
            action.accept(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Iterator<Entry<K, CacheEntry<K, V>>> it = cacheMap.entrySet().iterator();
        while (it.hasNext()) {
            CacheEntry<K, V> entry = it.next().getValue();
            if (entry.isExpire()) {
                it.remove();
                continue;
            }
            V newValue = function.apply(entry.getKey(), entry.getValue());
            entry.setValue(newValue);
        }
    }


    /**
     * 清理过期数据
     */
    public void clearExpire() {
        cacheMap.entrySet().removeIf((t) -> t.getValue().isExpire());
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
    public LazyCacheMap<K, V> clone() throws CloneNotSupportedException {
        clearExpire();
        LazyCacheMap<K, V> result;
        try {
            result = (LazyCacheMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
        if (!(cacheMap instanceof Cloneable)) {
            throw new CloneNotSupportedException("inner map don't implement cloneable!");
        }
        try {
            Method cloneMethod = cacheMap.getClass().getMethod("clone");
            result.cacheMap = (Map<K, CacheEntry<K, V>>) cloneMethod.invoke(cacheMap);
        } catch (NoSuchMethodException e) {
            throw new CloneNotSupportedException("inner map can not found clone method!");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CloneNotSupportedException("inner map invoke clone method error:" + e.getMessage());
        }

        result.entrySet = null;
        result.keySet = null;
        result.values = null;
        return result;
    }

    /**
     * 获取cacheEntry，如果过期则删除
     */
    private CacheEntry<K, V> getAndRemoveIfExpire(Object key) {
        CacheEntry<K, V> entry = cacheMap.get(key);
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
     * 移除entry
     */
    private CacheEntry<K, V> removeEntry(Object key) {
        CacheEntry<K, V> value = cacheMap.remove(key);
        if (value == null) {
            return null;
        }
        return value.isExpire() ? null : value;
    }

    /**
     * entry
     */
    private static class CacheEntry<K, V> extends SimpleEntry<K, V> {
        private static final long serialVersionUID = -8977888694870171813L;

        private Long expire;

        public CacheEntry(K key, V value, long expire) {
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
        public String toString() {
            return expire == null ?
                    getKey() + "=" + getValue() + "(Persistent)"
                    : getKey() + "=" + getValue() + "(" + ttl() + ")";

        }
    }


    final class KeySet extends AbstractSet<K> {
        public int size() {
            return LazyCacheMap.this.size();
        }

        public void clear() {
            LazyCacheMap.this.clear();
        }

        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        public boolean contains(Object o) {
            return LazyCacheMap.this.containsKey(o);
        }

        public boolean remove(Object o) {
            return removeEntry(o) != null;
        }

        public boolean removeIf(Predicate<? super K> filter) {
            return cacheMap.entrySet().removeIf((e) -> !e.getValue().isExpire() && filter.test(e.getValue().getKey()));
        }

        public void forEach(Consumer<? super K> action) {
            LazyCacheMap.this.forEach((k, v) -> action.accept(k));
        }

        public Spliterator<K> spliterator() {
            return new KeySpliterator();
        }

        public String toString() {
            Iterator<Entry<K, CacheEntry<K, V>>> it = cacheMap.entrySet().iterator();
            StringBuilder sb = new StringBuilder("{");
            while (it.hasNext()) {
                CacheEntry<K, V> entry = it.next().getValue();
                if (entry.isExpire()) {
                    it.remove();
                    continue;
                }
                sb.append(entry.getKey()).append(", ");
            }
            return sb.toString().endsWith("{") ?
                    "{}"
                    : sb.substring(0, sb.length() - 2) + "}";
        }
    }


    final class Values extends AbstractCollection<V> {
        public int size() {
            return LazyCacheMap.this.size();
        }

        public void clear() {
            LazyCacheMap.this.clear();
        }

        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        public boolean contains(Object o) {
            return LazyCacheMap.this.containsValue(o);
        }

        public boolean removeIf(Predicate<? super V> filter) {
            return cacheMap.entrySet().removeIf((e) -> !e.getValue().isExpire() && filter.test(e.getValue().getValue()));
        }

        public void forEach(Consumer<? super V> action) {
            LazyCacheMap.this.forEach((k, v) -> action.accept(v));
        }

        public Spliterator<V> spliterator() {
            return new ValueSpliterator();
        }

        public boolean remove(Object o) {
            Iterator<Entry<K, CacheEntry<K, V>>> it = cacheMap.entrySet().iterator();
            while (it.hasNext()) {
                CacheEntry<K, V> value = it.next().getValue();
                if (value.isExpire()) {
                    it.remove();
                    continue;
                }
                if (Objects.equals(o, value.getValue())) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            Iterator<Entry<K, CacheEntry<K, V>>> it = cacheMap.entrySet().iterator();
            StringBuilder sb = new StringBuilder("{");
            while (it.hasNext()) {
                CacheEntry<K, V> entry = it.next().getValue();
                if (entry.isExpire()) {
                    it.remove();
                    continue;
                }
                sb.append(entry.getValue()).append(", ");
            }
            return sb.toString().endsWith("{") ?
                    "{}"
                    : sb.substring(0, sb.length() - 2) + "}";
        }
    }


    final class EntrySet extends AbstractSet<Entry<K, V>> {
        public int size() {
            return LazyCacheMap.this.size();
        }

        public void clear() {
            LazyCacheMap.this.clear();
        }

        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        public String toString() {
            return LazyCacheMap.this.toString();
        }

        public boolean removeIf(Predicate<? super Entry<K, V>> filter) {
            return cacheMap.entrySet().removeIf((e) -> !e.getValue().isExpire() && filter.test(e.getValue()));
        }

        public Spliterator<Entry<K, V>> spliterator() {
            return new EntrySpliterator();
        }

        public boolean contains(Object o) {
            if (o instanceof Entry) {
                Entry<?, ?> entry = (Entry<?, ?>) o;
                CacheEntry<K, V> cacheEntry = getAndRemoveIfExpire(entry.getKey());
                return cacheEntry != null
                        && Objects.equals(cacheEntry.getValue(), entry.getValue());
            }
            return false;
        }

        public boolean remove(Object o) {
            if (o instanceof Entry) {
                Entry<?, ?> entry = (Entry<?, ?>) o;
                return LazyCacheMap.this.remove(entry.getKey(), entry.getValue());
            }
            return false;
        }

        public void forEach(Consumer<? super Entry<K, V>> action) {
            Iterator<Entry<K, CacheEntry<K, V>>> it = cacheMap.entrySet().iterator();
            while (it.hasNext()) {
                CacheEntry<K, V> entry = it.next().getValue();
                if (entry.isExpire()) {
                    it.remove();
                    continue;
                }
                action.accept(entry);
            }
        }
    }

    /**
     * 迭代器，迭代K,V
     * 当hasNext返回true时，next()拿到发现是过期数据，则自动获取下一个.
     * 如果最后一个仍是过期,则以key和value都为null的对象代替.
     */
    abstract class CacheIterator {
        private Iterator<K> cacheKeyIt;
        private ListIterator<CacheEntry<K, V>> cacheEntryIt;

        CacheIterator() {
            cacheKeyIt = cacheMap.keySet().iterator();
            cacheEntryIt = new ArrayList<>(cacheMap.values()).listIterator();
        }

        public final void remove() {
            cacheKeyIt.remove();
            cacheEntryIt.remove();
        }

        public final boolean hasNext() {
            if (cacheEntryIt.hasNext()) {
                CacheEntry<K, V> entry = cacheEntryIt.next();
                if (entry.isExpire()) {
                    cacheKeyIt.next();
                    remove();
                    return hasNext();
                }
                cacheEntryIt.previous();
                return true;
            }
            return false;
        }

        final Entry<K, V> nextEntry() {
            CacheEntry<K, V> entry = cacheEntryIt.next();
            cacheKeyIt.next();
            //如果已过期则取下一个，没有下一个则返回过期对象
            if (entry.isExpire() && hasNext()) {
                remove();
                return nextEntry();
            }
            return entry;
        }
    }

    class KeyIterator extends CacheIterator implements Iterator<K> {
        public K next() {
            return nextEntry().getKey();
        }
    }

    class ValueIterator extends CacheIterator implements Iterator<V> {
        public V next() {
            return nextEntry().getValue();
        }
    }

    class EntryIterator extends CacheIterator implements Iterator<Entry<K, V>> {
        public Entry<K, V> next() {
            return nextEntry();
        }
    }


    //--------Spliterator----
    abstract class CacheSpliterator {
        int index;
        int fence;
        final List<CacheEntry<K, V>> cacheList;

        CacheSpliterator() {
            clearExpire();
            this.cacheList = new ArrayList<>(cacheMap.values());
            index = 0;
            fence = cacheList.size();
        }

        CacheSpliterator(List<CacheEntry<K, V>> cacheList, int index, int fence) {
            this.cacheList = cacheList;
            this.index = index;
            this.fence = fence;
        }

        public long estimateSize() {
            return fence - index;
        }

        public int characteristics() {
            return Spliterator.SIZED | Spliterator.DISTINCT;
        }
    }


    final class KeySpliterator extends CacheSpliterator implements Spliterator<K> {
        KeySpliterator() {
            super();
        }

        private KeySpliterator(List<CacheEntry<K, V>> cacheList, int index, int fence) {
            super(cacheList, index, fence);
        }

        @Override
        public boolean tryAdvance(Consumer<? super K> action) {
            if (index < fence && index < cacheList.size()) {
                CacheEntry<K, V> entry = cacheList.get(index++);
                if (entry.isExpire()) {
                    return tryAdvance(action);
                }
                action.accept(entry.getKey());
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<K> trySplit() {
            int start = index, end = fence, mid = (start + end) >>> 1;
            return start >= mid ? null :
                    new KeySpliterator(cacheList, start, index = mid);
        }
    }

    final class ValueSpliterator extends CacheSpliterator implements Spliterator<V> {
        ValueSpliterator() {
            super();
        }

        private ValueSpliterator(List<CacheEntry<K, V>> cacheList, int index, int fence) {
            super(cacheList, index, fence);
        }

        @Override
        public boolean tryAdvance(Consumer<? super V> action) {
            if (index < fence && index < cacheList.size()) {
                CacheEntry<K, V> entry = cacheList.get(index++);
                if (entry.isExpire()) {
                    return tryAdvance(action);
                }
                action.accept(entry.getValue());
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<V> trySplit() {
            int start = index, end = fence, mid = (start + end) >>> 1;
            return start >= mid ? null :
                    new ValueSpliterator(cacheList, start, index = mid);
        }
    }

    final class EntrySpliterator extends CacheSpliterator implements Spliterator<Entry<K, V>> {
        EntrySpliterator() {
            super();
        }

        private EntrySpliterator(List<CacheEntry<K, V>> cacheList, int index, int fence) {
            super(cacheList, index, fence);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
            if (index < fence && index < cacheList.size()) {
                CacheEntry<K, V> entry = cacheList.get(index++);
                if (entry.isExpire()) {
                    return tryAdvance(action);
                }
                action.accept(entry);
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<Entry<K, V>> trySplit() {
            int start = index, end = fence, mid = (start + end) >>> 1;
            return start >= mid ? null :
                    new EntrySpliterator(cacheList, start, index = mid);
        }
    }


}



