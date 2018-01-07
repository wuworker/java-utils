package com.wxl.utils.map;

import com.wxl.utils.annotation.UnThreadSafe;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by wuxingle on 2017/12/30 0030.
 * 参考HashMap实现,还未加入红黑树实现部分
 * 延迟删除缓存map.
 * 当从map获取数据时，才清除过期数据.
 * 在对map进行遍历时,尽量使用forEach,
 * 因为Iterator可能拿到过期的数据
 */
@UnThreadSafe
public class LazyCacheMap<K, V> extends AbstractMap<K, V>
        implements CacheMap<K, V>, Serializable, Cloneable {

    private static final long serialVersionUID = -4240336930322646823L;

    private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;


    private transient CacheNode<K, V>[] table;

    private transient int size;

    private int modCount;

    private int threshold;

    private final float loadFactor;

    private transient Set<Entry<K, V>> entrySet;

    private transient Set<K> keySet;

    private transient Collection<V> values;


    public LazyCacheMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public LazyCacheMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public LazyCacheMap(int initialCapacity, float loadFactor) {
        Assert.isTrue(initialCapacity >= 0,
                "Illegal initial capacity: " + initialCapacity);
        Assert.isTrue(loadFactor > 0 && !Float.isNaN(loadFactor),
                "Illegal load factor: " + loadFactor);
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }

        this.threshold = tableSizeFor(initialCapacity);
        this.loadFactor = loadFactor;
    }

    public LazyCacheMap(Map<? extends K, ? extends V> map) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(map);
    }


    //----------------------------cacheMap实现-----------------------------------

    /**
     * 设置过期时间
     * 如果过期时间小于0,则删除
     */
    @Override
    public boolean setExpire(K key, long expire) {
        if (expire <= 0) {
            return removeNode(hash(key), key, null, false) != null;
        }
        CacheNode<K, V> node = getNode(hash(key), key);
        if (node == null) {
            return false;
        }
        node.expire = expire + currentTimeMillis();
        return true;
    }

    /**
     * 设置key为持久化
     */
    @Override
    public boolean setPersistent(K key) {
        CacheNode<K, V> node = getNode(hash(key), key);
        if (node == null) {
            return false;
        }
        node.expire = null;
        return true;
    }

    /**
     * 获取剩余存活时间
     *
     * @return PERSISTENT_KEY  说明是永不过期的key
     * NOT_EXIST_KEY   说明key不存在
     * 返回-1不一定是持久化的key,有可能正好过期1秒
     */
    @Override
    public long ttl(K key) {
        CacheNode<K, V> node = getNode(hash(key), key);
        if (node == null) {
            return NOT_EXIST_KEY;
        }
        Long expire = node.expire;
        if (expire == null) {
            return PERSISTENT_KEY;
        }
        return expire - currentTimeMillis();
    }

    /**
     * 判断是否是持久化的key
     */
    @Override
    public boolean isPersistent(K key) {
        CacheNode<K, V> node = getNode(hash(key), key);
        return node != null && node.expire == null;
    }

    /**
     * 放入并设置过期时间
     */
    @Override
    public V put(K key, V value, long expire) {
        if (expire <= 0) {
            CacheNode<K, V> node = removeNode(hash(key), key, null, false);
            return node == null ? null : node.value;
        }
        return putVal(hash(key), key, value, expire + currentTimeMillis(), false);
    }

    /**
     * @param expire 过期时间戳(将来的时间)
     */
    private V putVal(int hash, K key, V value, Long expire, boolean onlyIfAbsent) {
        CacheNode<K, V>[] tab = table;
        int len;
        CacheNode<K, V> p;
        if (tab == null || (len = tab.length) == 0) {
            len = (tab = resize()).length;
        }
        int index = hash & (len - 1);
        if ((p = tab[index]) == null) {
            tab[index] = new CacheNode<>(hash, key, value, expire, null);
        } else {
            CacheNode<K, V> existNode;
            if (p.hash == hash && Objects.equals(p.key, key)) {
                existNode = p;
            } else {
                while (true) {
                    if ((existNode = p.next) == null) {
                        p.next = new CacheNode<>(hash, key, value, expire, null);
                        break;
                    }
                    if (existNode.hash == hash && Objects.equals(existNode.key, key)) {
                        break;
                    }
                    p = existNode;
                }
            }
            //key已存在
            if (existNode != null) {
                //过期
                if (existNode.isExpire()) {
                    existNode.value = value;
                    existNode.expire = expire;
                    return null;
                }
                V old = existNode.value;
                if (old == null || !onlyIfAbsent) {
                    existNode.value = value;
                    existNode.expire = expire;
                }
                return old;
            }
        }
        ++modCount;
        if (++size > threshold)
            resize();
        return null;
    }

    /**
     * 重新计算大小,同时清理过期的key
     */
    private CacheNode<K, V>[] resize() {
        CacheNode<K, V>[] oldTab = table;
        int oldCap = oldTab == null ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
            //超出最大不扩容
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            //容量翻倍,(MAXIMUM_CAPACITY = 2^30,所以不会超出范围)
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY
                    && oldCap >= DEFAULT_INITIAL_CAPACITY) {
                newThr = oldThr << 1;
            }
        }
        //第一次初始化
        else if (oldThr > 0) {
            newCap = oldThr;
        }
        //调用构造方法把初始容量设置为了0
        else {
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float) newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY ?
                    (int) ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes", "unchecked"})
        CacheNode<K, V>[] newTab = (CacheNode<K, V>[]) (new CacheNode[newCap]);
        table = newTab;
        if (oldTab != null) {
            long now = currentTimeMillis();
            for (int i = 0; i < oldCap; i++) {
                CacheNode<K, V> node;
                if ((node = oldTab[i]) == null) {
                    continue;
                }
                oldTab[i] = null;
                if (node.next == null) {
                    //保存未过期的
                    if (!node.isExpire(now)) {
                        newTab[node.hash & (newCap - 1)] = node;
                    }
                }
                //有子节点
                else {
                    //head保存链表头,tail为游标
                    //lo表示不用换位置,hi表示需要换位置
                    CacheNode<K, V> loHead = null, loTail = null;
                    CacheNode<K, V> hiHead = null, hiTail = null;
                    CacheNode<K, V> next;
                    do {
                        next = node.next;
                        if (node.isExpire(now)) {
                            continue;
                        }
                        //oldCap一直为2次方,高位为1,其余位都是0,
                        //如果&后为0,说明扩容后不用换位置
                        if ((node.hash & oldCap) == 0) {
                            if (loTail == null) {
                                loHead = node;
                            } else {
                                loTail.next = node;
                            }
                            loTail = node;
                        } else {
                            if (hiTail == null) {
                                hiHead = node;
                            } else {
                                hiTail.next = node;
                            }
                            hiTail = node;
                        }
                    } while ((node = next) != null);

                    if (loTail != null) {
                        loTail.next = null;
                        newTab[i] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[i + oldCap] = hiHead;
                    }
                }
            }
        }

        return newTab;
    }

    @Override
    public V putIfAbsent(K key, V value, long expire) {
        if (expire <= 0) {
            CacheNode<K, V> node = removeNode(hash(key), key, value, true);
            return node == null ? null : node.value;
        }
        return putVal(hash(key), key, value, expire + currentTimeMillis(), true);
    }

    /**
     * 获取当前时间戳
     */
    private static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    //-------------------------map实现-------------------------------------------

    /**
     * 返回的size可能是个过期值
     * 调用size方法会遍历所有key,同时删除过期数据
     */
    @Override
    public int size() {
        clearIfExpire();
        return size;
    }

    /**
     * 清除所有过期的key
     */
    private void clearIfExpire() {
        CacheNode<K, V>[] tab;
        if ((tab = table) == null || size <= 0) {
            return;
        }
        long now = currentTimeMillis();
        for (int i = 0; i < tab.length; i++) {
            for (CacheNode<K, V> p = null, n = tab[i]; n != null; n = n.next) {
                //删除过期key
                if (n.isExpire(now)) {
                    if (p == null) {
                        tab[i] = n.next;
                    } else {
                        p.next = n.next;
                    }
                    modCount++;
                    size--;
                } else {
                    p = n;
                }
            }
        }
    }

    @Override
    public V get(Object key) {
        CacheNode<K, V> node = getNode(hash(key), key);
        return node == null ? null : node.value;
    }

    /**
     * 获取node，过期自动删除，返回null
     */
    private CacheNode<K, V> getNode(int hash, Object key) {
        CacheNode<K, V>[] tab = table;
        int len = tab == null ? 0 : tab.length;
        CacheNode<K, V> p;
        CacheNode<K, V> n;
        int index;
        if (len > 0 && (p = tab[index = hash & (len - 1)]) != null) {
            CacheNode<K, V> node = null;
            if (p.hash == hash && Objects.equals(p.key, key)) {
                node = p;
            } else if ((n = p.next) != null) {
                do {
                    if (n.hash == hash && Objects.equals(n.key, key)) {
                        node = n;
                        break;
                    }
                    p = n;
                } while ((n = n.next) != null);
            }
            //如果node过期,则删除
            if (node != null && node.isExpire()) {
                if (p == node) {
                    tab[index] = node.next;
                } else {
                    p.next = node.next;
                }
                ++modCount;
                --size;
                return null;
            } else {
                return node;
            }
        }
        return null;
    }


    @Override
    public boolean containsKey(Object key) {
        return getNode(hash(key), key) != null;
    }

    /**
     * 默认放入持久化的key
     */
    @Override
    public V put(K key, V value) {
        return putVal(hash(key), key, value, null, false);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        Assert.notNull(m, "putAll map can not null");
        putMapEntries(m);
    }

    @SuppressWarnings("unchecked")
    private void putMapEntries(Map<? extends K, ? extends V> m) {
        int s = m.size();
        if (s > 0) {
            if (table == null) {
                float ft = ((float) s / loadFactor) + 1.0F;
                int t = (ft < (float) MAXIMUM_CAPACITY ? (int) ft : MAXIMUM_CAPACITY);
                if (t > threshold) {
                    threshold = tableSizeFor(t);
                }
            } else if (s > threshold) {
                resize();
            }
            if (m instanceof LazyCacheMap) {
                for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
                    CacheNode<? extends K, ? extends V> cacheNode =
                            (CacheNode<? extends K, ? extends V>) entry;
                    K key = cacheNode.key;
                    V value = cacheNode.value;
                    Long expire = cacheNode.expire;
                    putVal(hash(key), key, value, expire, false);
                }
            } else {
                for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
                    K key = entry.getKey();
                    V value = entry.getValue();
                    putVal(hash(key), key, value, null, false);
                }
            }
        }
    }


    @Override
    public V remove(Object key) {
        CacheNode<K, V> node = removeNode(hash(key), key, null, false);
        return node == null ? null : node.value;
    }


    private CacheNode<K, V> removeNode(int hash, Object key, Object value, boolean matchValue) {
        CacheNode<K, V>[] tab = table;
        int len = tab == null ? 0 : tab.length;
        CacheNode<K, V> p;
        CacheNode<K, V> n;
        int index;
        if (len > 0 && (p = tab[index = hash & (len - 1)]) != null) {
            CacheNode<K, V> node = null;
            if (p.hash == hash && Objects.equals(p.key, key)) {
                node = p;
            } else if ((n = p.next) != null) {
                do {
                    if (n.hash == hash && Objects.equals(n.key, key)) {
                        node = n;
                        break;
                    }
                    p = n;
                } while ((n = n.next) != null);
            }

            if (node != null) {
                //已过期,不返回
                if (node.isExpire()) {
                    if (node == p) {
                        tab[index] = node.next;
                    } else {
                        p.next = node.next;
                    }
                    ++modCount;
                    --size;
                    return null;
                }
                //删除并返回
                if (!matchValue || Objects.equals(node.value, value)) {
                    if (node == p) {
                        tab[index] = node.next;
                    } else {
                        p.next = node.next;
                    }
                    ++modCount;
                    --size;
                    return node;
                }
            }
        }
        return null;
    }


    @Override
    public void clear() {
        CacheNode<K, V>[] tab;
        ++modCount;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; i++) {
                tab[i] = null;
            }
        }
    }

    /**
     * 遍历找value
     * 如果value过期则删除,并继续找一下个
     */
    @Override
    public boolean containsValue(Object value) {
        CacheNode<K, V>[] tab;
        if ((tab = table) == null || size <= 0) {
            return false;
        }
        long now = currentTimeMillis();
        for (int i = 0; i < tab.length; i++) {
            for (CacheNode<K, V> p = null, n = tab[i]; n != null; n = n.next) {
                //过期删除
                if (n.isExpire(now)) {
                    if (p == null) {
                        tab[i] = n.next;
                    } else {
                        p.next = n.next;
                    }
                    modCount++;
                    size--;
                } else {
                    if (Objects.equals(n.value, value)) {
                        return true;
                    }
                    p = n;
                }
            }
        }
        return false;
    }


    @Override
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            keySet = ks = new KeySet();
        }
        return ks;
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
            return removeNode(hash(o), o, null, false) != null;
        }

        public Spliterator<K> spliterator() {
            return new KeySpliterator<>(LazyCacheMap.this, 0, -1, 0, 0);
        }

        public void forEach(Consumer<? super K> action) {
            CacheNode<K, V>[] tab;
            if ((tab = table) == null || size <= 0) {
                return;
            }
            long now = currentTimeMillis();
            int mod = modCount;
            for (int i = 0; i < tab.length; i++) {
                for (CacheNode<K, V> p = null, n = tab[i]; n != null; n = n.next) {
                    //过期删除
                    if (n.isExpire(now)) {
                        if (p == null) {
                            tab[i] = n.next;
                        } else {
                            p.next = n.next;
                        }
                        modCount++;
                        mod++;
                        size--;
                    } else {
                        action.accept(n.key);
                        p = n;
                    }
                }
            }
            if (mod != modCount) {
                throw new ConcurrentModificationException();
            }
        }

    }

    @Override
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            values = vs = new Values();
        }
        return vs;
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

        public Spliterator<V> spliterator() {
            return new ValueSpliterator<>(LazyCacheMap.this, 0, -1, 0, 0);
        }

        public void forEach(Consumer<? super V> action) {
            CacheNode<K, V>[] tab;
            if ((tab = table) == null || size <= 0) {
                return;
            }
            long now = currentTimeMillis();
            int mod = modCount;
            for (int i = 0; i < tab.length; i++) {
                for (CacheNode<K, V> p = null, n = tab[i]; n != null; n = n.next) {
                    //过期删除
                    if (n.isExpire(now)) {
                        if (p == null) {
                            tab[i] = n.next;
                        } else {
                            p.next = n.next;
                        }
                        modCount++;
                        mod++;
                        size--;
                    } else {
                        action.accept(n.value);
                        p = n;
                    }
                }
            }
            if (mod != modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }


    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> es = entrySet;
        if (es == null) {
            entrySet = es = new EntrySet();
        }
        return es;
    }


    final class EntrySet extends AbstractSet<Entry<K, V>> {

        public int size() {
            return LazyCacheMap.this.size();
        }

        public void clear() {
            LazyCacheMap.this.clear();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> entry = (Entry<?, ?>) o;
            Object key = entry.getKey();
            CacheEntry<K, V> cacheEntry = getNode(hash(key), key);
            return cacheEntry != null && cacheEntry.equals(entry);
        }

        public boolean remove(Object o) {
            if (o instanceof Entry) {
                Entry<?, ?> entry = (Entry<?, ?>) o;
                Object key = entry.getKey();
                Object value = entry.getValue();
                return removeNode(hash(key), key, value, true) != null;
            }
            return false;
        }

        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        public Spliterator<Entry<K, V>> spliterator() {
            return new EntrySpliterator<>(LazyCacheMap.this, 0, -1, 0, 0);
        }

        public void forEach(Consumer<? super Entry<K, V>> action) {
            CacheNode<K, V>[] tab;
            if ((tab = table) == null || size <= 0) {
                return;
            }
            long now = currentTimeMillis();
            int mod = modCount;
            for (int i = 0; i < tab.length; i++) {
                for (CacheNode<K, V> p = null, n = tab[i]; n != null; n = n.next) {
                    //过期删除
                    if (n.isExpire(now)) {
                        if (p == null) {
                            tab[i] = n.next;
                        } else {
                            p.next = n.next;
                        }
                        modCount++;
                        mod++;
                        size--;
                    } else {
                        action.accept(n);
                        p = n;
                    }
                }
            }
            if (mod != modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }


    private int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 返回大于cap的2的整数次幂
     */
    private int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }


    //---------------------------------jdk8--------------------------------------


    @Override
    public V getOrDefault(Object key, V defaultValue) {
        CacheNode<K, V> cacheNode = getNode(hash(key), key);
        return cacheNode == null ? defaultValue : cacheNode.value;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return putVal(hash(key), key, value, null, true);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return removeNode(hash(key), key, value, true) != null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        CacheNode<K, V> node = getNode(hash(key), key);
        if (node != null && Objects.equals(node.value, oldValue)) {
            node.value = newValue;
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, V value) {
        CacheNode<K, V> node = getNode(hash(key), key);
        if (node != null) {
            V old = node.value;
            node.value = value;
            return old;
        }
        return null;
    }

    /**
     * 当oldValue不存在时,使用新的value代替
     * <pre> {@code
     * if (map.get(key) == null) {
     *     V newValue = mappingFunction.apply(key);
     *     if (newValue != null)
     *         map.put(key, newValue);
     * }
     * }</pre>
     */
    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Assert.notNull(mappingFunction, "function can not null");
        int hash = hash(key);
        CacheNode<K, V>[] tab;
        CacheNode<K, V> first;
        CacheNode<K, V> old = null;
        CacheNode<K, V> pre = null;
        int len, index;
        //resize
        if ((tab = table) == null || (len = tab.length) == 0 || size > threshold) {
            len = (tab = resize()).length;
        }
        long now = currentTimeMillis();
        //找到oldNode
        if ((first = tab[index = hash & (len - 1)]) != null) {
            CacheNode<K, V> n = first;
            do {
                if (n.hash == hash && Objects.equals(n.key, key)) {
                    old = n;
                    break;
                }
                pre = n;
            } while ((n = n.next) != null);
            //old已存在
            if (old != null && !old.isExpire(now) && old.value != null) {
                return old.value;
            }
        }
        //old过期
        if (old != null && old.isExpire(now)) {
            if (pre == null) {
                first = tab[index] = old.next;
            } else {
                pre.next = old.next;
            }
            modCount++;
            size--;
            old = null;
        }
        V newVal = mappingFunction.apply(key);
        if (newVal == null) {
            return null;
        } else if (old != null) {
            old.value = newVal;
            return newVal;
        } else {
            tab[index] = new CacheNode<>(hash, key, newVal, null, first);
            modCount++;
            size++;
            return newVal;
        }
    }

    /**
     * 当oldValue存在时,使用新的value进行替代
     * <pre> {@code
     * if (map.get(key) != null) {
     *     V oldValue = map.get(key);
     *     V newValue = remappingFunction.apply(key, oldValue);
     *     if (newValue != null)
     *         map.put(key, newValue);
     *     else
     *         map.remove(key);
     * }
     * }</pre>
     */
    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Assert.notNull(remappingFunction, "function can not null");
        int hash = hash(key);
        CacheNode<K, V> node;
        V oldVal;
        if ((node = getNode(hash, key)) != null) {
            //删除过期
            if (node.isExpire()) {
                removeNode(hash, key, null, false);
                return null;
            }
            if ((oldVal = node.value) != null) {
                V newVal = remappingFunction.apply(key, oldVal);
                if (newVal != null) {
                    node.value = newVal;
                    return newVal;
                } else {
                    removeNode(hash, key, null, false);
                }
            }
        }
        return null;
    }

    /**
     * 不管oldValue是否存在,都使用newValue进行替换
     * <pre> {@code
     * V oldValue = map.get(key);
     * V newValue = remappingFunction.apply(key, oldValue);
     * if (oldValue != null ) {
     *    if (newValue != null)
     *       map.put(key, newValue);
     *    else
     *       map.remove(key);
     * } else {
     *    if (newValue != null)
     *       map.put(key, newValue);
     *    else
     *       return null;
     * }
     * }</pre>
     */
    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Assert.notNull(remappingFunction, "function can not null");
        int hash = hash(key);
        CacheNode<K, V>[] tab;
        CacheNode<K, V> first;
        CacheNode<K, V> old = null;
        CacheNode<K, V> pre = null;
        int len, index;
        //resize
        if ((tab = table) == null || (len = tab.length) == 0 || size > threshold) {
            len = (tab = resize()).length;
        }
        //找到oldNode
        if ((first = tab[index = hash & (len - 1)]) != null) {
            CacheNode<K, V> n = first;
            do {
                if (n.hash == hash && Objects.equals(n.key, key)) {
                    old = n;
                    break;
                }
                pre = n;
            } while ((n = n.next) != null);
        }
        //old过期
        if (old != null && old.isExpire()) {
            if (pre == null) {
                first = tab[index] = old.next;
            } else {
                pre.next = old.next;
            }
            modCount++;
            size--;
            old = null;
        }
        V oldVal = old == null ? null : old.value;
        V newVal = remappingFunction.apply(key, oldVal);
        if (old != null) {
            if (newVal != null) {
                old.value = newVal;
            }
            //删除old
            else {
                if (pre == null) {
                    tab[index] = old.next;
                } else {
                    pre.next = old.next;
                }
                modCount++;
                size--;
            }
        } else if (newVal != null) {
            tab[index] = new CacheNode<>(hash, key, newVal, null, first);
            modCount++;
            size++;
        }
        return newVal;
    }

    /**
     * oldValue不存在时,使用默认值value
     * 否则进行remappingFunction.apply(oldValue, value)计算newValue
     * <pre> {@code
     * V oldValue = map.get(key);
     * V newValue = (oldValue == null) ? value :
     *              remappingFunction.apply(oldValue, value);
     * if (newValue == null)
     *     map.remove(key);
     * else
     *     map.put(key, newValue);
     * }</pre>
     */
    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Assert.notNull(remappingFunction, "function can not null");
        int hash = hash(key);
        CacheNode<K, V>[] tab;
        CacheNode<K, V> first;
        CacheNode<K, V> old = null;
        CacheNode<K, V> pre = null;
        int len, index;
        //resize
        if ((tab = table) == null || (len = tab.length) == 0 || size > threshold) {
            len = (tab = resize()).length;
        }
        //找到oldNode
        if ((first = tab[index = hash & (len - 1)]) != null) {
            CacheNode<K, V> n = first;
            do {
                if (n.hash == hash && Objects.equals(n.key, key)) {
                    old = n;
                    break;
                }
                pre = n;
            } while ((n = n.next) != null);
        }
        //old过期
        if (old != null && old.isExpire()) {
            if (pre == null) {
                first = tab[index] = old.next;
            } else {
                pre.next = old.next;
            }
            old = null;
            modCount++;
            size--;
        }

        if (old != null) {
            V newVal;
            if (old.value != null) {
                newVal = remappingFunction.apply(old.value, value);
            } else {
                newVal = value;
            }
            if (newVal != null) {
                old.value = newVal;
            }
            //删除
            else {
                if (pre == null) {
                    tab[index] = old.next;
                } else {
                    pre.next = old.next;
                }
                modCount++;
                size--;
            }
        } else if (value != null) {
            tab[index] = new CacheNode<>(hash, key, value, null, first);
            modCount++;
            size++;
        }
        return value;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Assert.notNull(action, "consumer can not null");
        CacheNode<K, V>[] tab;
        if ((tab = table) == null || size <= 0) {
            return;
        }
        long now = currentTimeMillis();
        int mod = modCount;
        for (int i = 0; i < tab.length; i++) {
            for (CacheNode<K, V> p = null, n = tab[i]; n != null; n = n.next) {
                //删除过期key
                if (n.isExpire(now)) {
                    if (p == null) {
                        tab[i] = n.next;
                    } else {
                        p.next = n.next;
                    }
                    modCount++;
                    mod++;
                    size--;
                } else {
                    action.accept(n.key, n.value);
                    p = n;
                }
            }
        }
        if (mod != modCount) {
            throw new ConcurrentModificationException();
        }

    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Assert.notNull(function, "function can not null");
        CacheNode<K, V>[] tab;
        if ((tab = table) == null || size <= 0) {
            return;
        }
        long now = currentTimeMillis();
        int mod = modCount;
        for (int i = 0; i < tab.length; i++) {
            for (CacheNode<K, V> p = null, n = tab[i]; n != null; n = n.next) {
                //删除过期key
                if (n.isExpire(now)) {
                    if (p == null) {
                        tab[i] = n.next;
                    } else {
                        p.next = n.next;
                    }
                    modCount++;
                    mod++;
                    size--;
                } else {
                    n.value = function.apply(n.key, n.value);
                    p = n;
                }
            }
        }
        if (mod != modCount) {
            throw new ConcurrentModificationException();
        }
    }


    //-----------------------------------Object-------------------------------------------

    /**
     * toString
     * 同时打印过期时间
     */
    @Override
    public String toString() {
        CacheNode<K, V>[] tab;
        if ((tab = table) == null || size <= 0) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        long now = currentTimeMillis();
        int mod = modCount;
        for (int i = 0; i < tab.length; i++) {
            for (CacheNode<K, V> p = null, n = tab[i]; n != null; n = n.next) {
                //删除过期key
                if (n.isExpire(now)) {
                    if (p == null) {
                        tab[i] = n.next;
                    } else {
                        p.next = n.next;
                    }
                    modCount++;
                    mod++;
                    size--;
                } else {
                    K key = n.key;
                    V value = n.value;
                    Long expire = n.expire;
                    sb.append(key == this ? "(this Map)" : key)
                            .append("=")
                            .append(value == this ? "(this Map)" : value)
                            .append("(")
                            .append(expire == null ? "persistent" : expire - now)
                            .append(")")
                            .append(",");
                    p = n;
                }
            }
        }
        if (mod != modCount) {
            throw new ConcurrentModificationException();
        }
        return sb.length() == 1 ?
                sb.append("}").toString()
                : sb.substring(0, sb.length() - 1) + "}";
    }

    @Override
    @SuppressWarnings("unchecked")
    public LazyCacheMap clone() {
        LazyCacheMap<K, V> map;
        try {
            map = (LazyCacheMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        map.reinitialize();
        map.putMapEntries(this);

        return map;
    }

    private void reinitialize() {
        table = null;
        entrySet = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }


    //-----------------------------------------serializable------------------------------

    private void writeObject(ObjectOutputStream s)
            throws IOException {
        //清理过期数据
        clearIfExpire();

        s.defaultWriteObject();

        s.writeInt(size);
        internalWriteEntries(s);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        reinitialize();

        Assert.isTrue(loadFactor > 0 && !Float.isNaN(loadFactor),
                "Illegal load factor: " + loadFactor);

        int size = s.readInt();
        Assert.isTrue(size >= 0, "Illegal size: " + size);

        if (size > 0) {
            //计算容量和threshold
            float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
            float fc = (float) size / lf + 1.0f;
            int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                    DEFAULT_INITIAL_CAPACITY :
                    (fc >= MAXIMUM_CAPACITY) ?
                            MAXIMUM_CAPACITY :
                            tableSizeFor((int) fc));
            float ft = (float) cap * lf;
            threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                    (int) ft : Integer.MAX_VALUE);
            table = (CacheNode<K, V>[]) new CacheNode[cap];
            for (int i = 0; i < size; i++) {
                K key = (K) s.readObject();
                V value = (V) s.readObject();
                Long expire = (Long) s.readObject();
                putVal(hash(key), key, value, expire, false);
            }
        }
    }


    private void internalWriteEntries(ObjectOutputStream s) throws IOException {
        CacheNode<K, V>[] tab;
        if ((size <= 0) || (tab = table) == null) {
            return;
        }
        for (int i = 0; i < tab.length; i++) {
            for (CacheNode<K, V> n = tab[i]; n != null; n = n.next) {
                s.writeObject(n.key);
                s.writeObject(n.value);
                s.writeObject(n.expire);
            }
        }
    }

    //------------------------------------------node-------------------------------------------

    /**
     * node,增加了过期时间
     * expire为null说明永不过期
     */
    static class CacheNode<K, V> implements CacheEntry<K, V> {

        final int hash;
        final K key;
        V value;
        //过期时间
        Long expire;
        CacheNode<K, V> next;

        CacheNode(int hash, K key, V value, Long expire, CacheNode<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.expire = expire;
            this.next = next;
        }

        @Override
        public boolean isExpire() {
            return isExpire(currentTimeMillis());
        }

        boolean isExpire(long now) {
            return expire != null && expire - now <= 0;
        }

        public Long getExpire() {
            return expire;
        }

        public void setExpire(Long expire) {
            this.expire = expire;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }

        public int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof Entry) {
                Entry<?, ?> entry = (Entry<?, ?>) obj;
                return Objects.equals(key, entry.getKey())
                        && Objects.equals(value, entry.getValue());
            }
            return false;
        }

        public String toString() {
            return "CacheNode{" +
                    "hash=" + hash +
                    ", key=" + key +
                    ", value=" + value +
                    ", expire=" + (expire == null ? null : expire - currentTimeMillis()) +
                    ", next=" + next +
                    '}';
        }
    }

    //--------------------------------------Iterator--------------------------------------

    /**
     * 在遍历时,会删除过期的key,并指向一下个不过期的key
     * 当调用next时,如果key已过期并且是最后一个元素,那么返回过期的key
     * 否则继续寻找下一个没过期的key
     * <pre> {@code
     * Iterator it = map.entrySet().iterator();
     * while(it.hasNext()){
     *     Thread.sleep(1000);
     *     //当entry已过期,并且是最后一个元素
     *     //那么拿到的是一个过期的entry
     *     Entry entry = it.next();
     * }
     * }</pre>
     */
    private class CacheIterator {
        CacheNode<K, V> next;
        CacheNode<K, V> current;
        int expectedModCount;
        int index;

        CacheIterator() {
            CacheNode<K, V>[] tab = table;
            expectedModCount = modCount;
            next = current = null;
            index = 0;
            if (tab != null && size > 0) {
                //找到第一个不为null的
                do {
                } while (index < tab.length && (next = tab[index++]) == null);
            }
        }

        public final boolean hasNext() {
            CacheNode<K, V> n = next;
            if (n == null) {
                return false;
            }
            long now = currentTimeMillis();
            //如果已过期,则删除并继续下一个
            while (n.isExpire(now)) {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                K key = n.key;
                removeNode(hash(key), key, null, false);
                expectedModCount = modCount;

                n = n.next;
                if (n == null) {
                    CacheNode<K, V>[] tab = table;
                    do {
                    } while (index < tab.length && (n = tab[index++]) == null);
                    if (n == null) {
                        next = null;
                        return false;
                    }
                }
            }
            next = n;
            return true;
        }

        public final CacheNode<K, V> nextNode() {
            CacheNode<K, V>[] t;
            CacheNode<K, V> e = next;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (e == null) {
                throw new NoSuchElementException();
            }
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {
                } while (index < t.length && (next = t[index++]) == null);
            }
            //如果当期已过期,并且还有下一个元素
            if (e.isExpire() && hasNext()) {
                K key = e.key;
                removeNode(hash(key), key, null, false);
                expectedModCount = modCount;

                e = next;
                if (e == null) {
                    throw new NoSuchElementException();
                }
                if ((next = (current = e).next) == null && (t = table) != null) {
                    do {
                    } while (index < t.length && (next = t[index++]) == null);
                }
                return e;
            }
            return e;
        }

        public final void remove() {
            CacheNode<K, V> p = current;
            if (p == null) {
                throw new IllegalStateException();
            }
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            K key = p.key;
            removeNode(hash(key), key, null, false);
            expectedModCount = modCount;
        }
    }


    final class KeyIterator extends CacheIterator implements Iterator<K> {
        @Override
        public K next() {
            return nextNode().key;
        }
    }

    final class ValueIterator extends CacheIterator implements Iterator<V> {
        @Override
        public V next() {
            return nextNode().value;
        }
    }

    final class EntryIterator extends CacheIterator implements Iterator<Entry<K, V>> {
        @Override
        public Entry<K, V> next() {
            return nextNode();
        }
    }


    //----------------------------spliterators----------------------------------------

    /**
     * 因为spliterator可以用在并发,
     * 所以在遍历时会忽略过期的key,
     * 但并不会进行删除
     */
    static class LazyCacheMapSpliterators<K, V> {
        final LazyCacheMap<K, V> map;
        CacheNode<K, V> current;
        //当前index
        int index;
        //last index
        int fence;
        //estimate size
        int est;
        int expectedModCount;

        LazyCacheMapSpliterators(LazyCacheMap<K, V> map, int index, int fence,
                                 int est, int expectedModCount) {
            this.map = map;
            this.index = index;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() {
            int hi;
            if ((hi = fence) < 0) {
                LazyCacheMap<K, V> cacheMap = map;
                est = cacheMap.size;
                expectedModCount = cacheMap.modCount;
                CacheNode<K, V>[] tab = cacheMap.table;
                hi = fence = (tab == null ? 0 : tab.length);
            }
            return hi;
        }

        public final long estimateSize() {
            getFence();
            return (long) est;
        }
    }

    static class KeySpliterator<K, V> extends LazyCacheMapSpliterators<K, V>
            implements Spliterator<K> {

        KeySpliterator(LazyCacheMap<K, V> map, int index,
                       int fence, int est, int expectedModCount) {
            super(map, index, fence, est, expectedModCount);
        }

        @Override
        public void forEachRemaining(Consumer<? super K> action) {
            Assert.notNull(action, "action can not null");
            LazyCacheMap<K, V> cacheMap = map;
            CacheNode<K, V>[] tab = cacheMap.table;
            int hi, mc, i;
            if ((hi = fence) < 0) {
                hi = tab == null ? 0 : tab.length;
                mc = expectedModCount = cacheMap.modCount;
            } else {
                mc = expectedModCount;
            }
            if (tab != null && tab.length >= hi && (i = index) >= 0
                    && (i < (index = hi) || current != null)) {
                CacheNode<K, V> n = current;
                current = null;
                long now = currentTimeMillis();
                do {
                    if (n == null) {
                        n = tab[i++];
                    } else {
                        //略过过期key
                        if (!n.isExpire(now)) {
                            action.accept(n.key);
                        }
                        n = n.next;
                    }
                } while (n != null || i < hi);
                if (mc != cacheMap.modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super K> action) {
            Assert.notNull(action, "action can not null");
            CacheNode<K, V>[] tab = map.table;
            int hi;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                long now = currentTimeMillis();
                while (current != null || index < hi) {
                    if (current == null) {
                        current = tab[index++];
                    } else {
                        if (!current.isExpire(now)) {
                            K k = current.key;
                            action.accept(k);
                            if (map.modCount != expectedModCount) {
                                throw new ConcurrentModificationException();
                            }
                            return true;
                        }
                        current = current.next;
                    }
                }
            }
            return false;
        }

        @Override
        public Spliterator<K> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        @Override
        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                    Spliterator.DISTINCT;
        }
    }

    static class ValueSpliterator<K, V> extends LazyCacheMapSpliterators<K, V>
            implements Spliterator<V> {

        ValueSpliterator(LazyCacheMap<K, V> map, int index,
                         int fence, int est, int expectedModCount) {
            super(map, index, fence, est, expectedModCount);
        }

        @Override
        public void forEachRemaining(Consumer<? super V> action) {
            Assert.notNull(action, "action can not null");
            LazyCacheMap<K, V> cacheMap = map;
            CacheNode<K, V>[] tab = cacheMap.table;
            int hi, mc, i;
            if ((hi = fence) < 0) {
                hi = tab == null ? 0 : tab.length;
                mc = expectedModCount = cacheMap.modCount;
            } else {
                mc = expectedModCount;
            }
            if (tab != null && tab.length >= hi && (i = index) >= 0
                    && (i < (index = hi) || current != null)) {
                CacheNode<K, V> n = current;
                current = null;
                long now = currentTimeMillis();
                do {
                    if (n == null) {
                        n = tab[i++];
                    } else {
                        //略过过期key
                        if (!n.isExpire(now)) {
                            action.accept(n.value);
                        }
                        n = n.next;
                    }
                } while (n != null || i < hi);
                if (mc != cacheMap.modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super V> action) {
            Assert.notNull(action, "action can not null");
            CacheNode<K, V>[] tab = map.table;
            int hi;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                long now = currentTimeMillis();
                while (current != null || index < hi) {
                    if (current == null) {
                        current = tab[index++];
                    } else {
                        if (!current.isExpire(now)) {
                            V v = current.value;
                            action.accept(v);
                            if (map.modCount != expectedModCount) {
                                throw new ConcurrentModificationException();
                            }
                            return true;
                        }
                        current = current.next;
                    }
                }
            }
            return false;
        }

        @Override
        public Spliterator<V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new ValueSpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        @Override
        public int characteristics() {
            return fence < 0 || est == map.size ? Spliterator.SIZED : 0;
        }
    }


    static class EntrySpliterator<K, V> extends LazyCacheMapSpliterators<K, V>
            implements Spliterator<Entry<K, V>> {

        EntrySpliterator(LazyCacheMap<K, V> map, int index,
                         int fence, int est, int expectedModCount) {
            super(map, index, fence, est, expectedModCount);
        }

        @Override
        public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
            Assert.notNull(action, "action can not null");
            LazyCacheMap<K, V> cacheMap = map;
            CacheNode<K, V>[] tab = cacheMap.table;
            int hi, mc, i;
            if ((hi = fence) < 0) {
                hi = tab == null ? 0 : tab.length;
                mc = expectedModCount = cacheMap.modCount;
            } else {
                mc = expectedModCount;
            }
            if (tab != null && tab.length >= hi && (i = index) >= 0
                    && (i < (index = hi) || current != null)) {
                CacheNode<K, V> n = current;
                current = null;
                long now = currentTimeMillis();
                do {
                    if (n == null) {
                        n = tab[i++];
                    } else {
                        //略过过期key
                        if (!n.isExpire(now)) {
                            action.accept(n);
                        }
                        n = n.next;
                    }
                } while (n != null || i < hi);
                if (mc != cacheMap.modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
            Assert.notNull(action, "action can not null");
            CacheNode<K, V>[] tab = map.table;
            int hi;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                long now = currentTimeMillis();
                while (current != null || index < hi) {
                    if (current == null) {
                        current = tab[index++];
                    } else {
                        if (!current.isExpire(now)) {
                            CacheNode<K, V> e = current;
                            action.accept(e);
                            if (map.modCount != expectedModCount) {
                                throw new ConcurrentModificationException();
                            }
                            return true;
                        }
                        current = current.next;
                    }
                }
            }
            return false;
        }

        @Override
        public Spliterator<Entry<K, V>> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new EntrySpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        @Override
        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                    Spliterator.DISTINCT;
        }
    }
}

