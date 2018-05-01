package com.wxl.utils.collection;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Create by wuxingle on 2018/5/1
 * 键值对
 */
@Setter
@Getter
public class KeyValue<K, V> implements Serializable {

    private static final long serialVersionUID = 7791077991110312636L;

    private K key;

    private V value;

    public KeyValue() {
    }

    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof KeyValue) {
            KeyValue keyValue = (KeyValue) obj;
            return Objects.equals(keyValue.key, key)
                    && Objects.equals(keyValue.value, value);
        }
        return false;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

}
