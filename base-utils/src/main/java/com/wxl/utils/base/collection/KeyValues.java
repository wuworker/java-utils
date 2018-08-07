package com.wxl.utils.base.collection;

import java.util.Arrays;
import java.util.List;

/**
 * Create by wuxingle on 2018/5/1
 * 键值对，值是list
 */
public class KeyValues<K, V> extends KeyValue<K, List<V>> {

    private static final long serialVersionUID = -2090131280840617239L;

    public KeyValues() {
    }

    public KeyValues(K key, V... value) {
        this(key, Arrays.asList(value));
    }

    public KeyValues(K key, List<V> value) {
        super(key, value);
    }

    public int size() {
        return getValue().size();
    }

    public V getFirst() {
        List<V> vs = getValue();
        return vs.isEmpty() ? null : vs.get(0);
    }

    public String toValueString(String split) {
        StringBuilder sb = new StringBuilder();
        for (V v : getValue()) {
            sb.append(v).append(split);
        }
        return sb.substring(0, sb.length() - split.length());
    }

}
