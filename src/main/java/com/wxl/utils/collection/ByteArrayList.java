package com.wxl.utils.collection;

import com.wxl.utils.IOUtils;
import com.wxl.utils.annotation.UnThreadSafe;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Create by wuxingle on 2018/4/5
 * 自动扩容的byte数组集合
 */
@UnThreadSafe
public class ByteArrayList implements Iterable<Byte>, Serializable, Cloneable {

    private static final long serialVersionUID = 301869012058883782L;

    private static final int DEFAULT_INIT_SIZE = 256;

    private byte[] array;

    private int size;

    public ByteArrayList() {
        this(DEFAULT_INIT_SIZE);
    }

    public ByteArrayList(int size) {
        array = new byte[size];
    }

    public ByteArrayList(byte[] data) {
        this(data, 0, data.length);
    }

    public ByteArrayList(byte[] data, int start, int len) {
        rangeCheckForBytes(data, start, len);
        array = new byte[len];
        System.arraycopy(data, start, array, 0, len);
        size = len;
    }

    /**
     * 从输入流中获取
     */
    public static ByteArrayList fromStream(InputStream in) throws IOException {
        return fromStream(in, DEFAULT_INIT_SIZE);
    }

    public static ByteArrayList fromStream(InputStream in, int initSize) throws IOException {
        return new ByteArrayList(IOUtils.toByte(in, initSize));
    }

    /**
     * 数组大小
     */
    public int size() {
        return size;
    }

    /**
     * 获取
     */
    public byte get(int index) {
        rangeCheck(index);
        return array[index];
    }


    /**
     * 返回找到的第一个索引
     */
    public int indexOf(int b) {
        for (int i = 0; i < size; i++) {
            if (array[i] == b) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(byte[] bytes) {
        return indexOf(bytes, 0, bytes.length);
    }

    public int indexOf(byte[] bytes, int start, int len) {
        rangeCheckForBytes(bytes, start, len);
        if (len > size) {
            return -1;
        }
        int s = start;
        for (int i = 0; i < size; i++) {
            if (array[i] == bytes[s++]) {
                if (s >= start + len) {
                    return i - len + 1;
                }
                continue;
            }
            s = start;
        }
        return -1;
    }

    /**
     * 从后往前找，找到的第一个索引
     */
    public int lastIndexOf(int b) {
        for (int i = size - 1; i >= 0; i--) {
            if (array[i] == b) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(byte[] bytes) {
        return lastIndexOf(bytes, 0, bytes.length);
    }

    public int lastIndexOf(byte[] bytes, int start, int len) {
        rangeCheckForBytes(bytes, start, len);
        if (len > size) {
            return -1;
        }
        int init = start + len - 1;
        int s = init;
        for (int i = size - 1; i >= 0; i--) {
            if (array[i] == bytes[s--]) {
                if (s < start) {
                    return i;
                }
                continue;
            }
            s = init;
        }
        return -1;
    }


    /**
     * 是否包含元素
     */
    public boolean contains(byte b) {
        return indexOf(b) > 0;
    }

    public boolean contains(byte[] bytes) {
        return indexOf(bytes) > 0;
    }

    public boolean contains(byte[] bytes, int start, int len) {
        return indexOf(bytes, start, len) > 0;
    }


    /**
     * 添加
     *
     * @return size 索引
     */
    public int add(int b) {
        resize();
        array[size] = (byte) b;
        return size++;
    }

    public int addAll(byte[] bytes) {
        return addAll(bytes, 0, bytes.length);
    }

    public int addAll(byte[] bytes, int start, int len) {
        rangeCheckForBytes(bytes, start, len);
        if (array.length - size < len) {
            byte[] tmp = new byte[len + size];
            System.arraycopy(array, 0, tmp, 0, size);
            System.arraycopy(bytes, start, tmp, size, len);
            array = tmp;
        } else {
            System.arraycopy(bytes, start, array, size, len);
        }
        size += len;
        return size - 1;
    }

    /**
     * 添加在指定索引处
     */
    public int add(int index, int b) {
        rangeCheckForAdd(index);
        resize();
        System.arraycopy(array, index, array, index + 1, size - index);
        array[index] = (byte) b;
        size++;
        return 1;
    }

    public int addAll(int index, byte[] bytes) {
        return addAll(index, bytes, 0, bytes.length);
    }

    public int addAll(int index, byte[] bytes, int start, int len) {
        rangeCheckForAdd(index);
        rangeCheckForBytes(bytes, start, len);
        if (array.length - size < len) {
            byte[] tmp = new byte[len + size];
            System.arraycopy(array, 0, tmp, 0, index);
            System.arraycopy(bytes, start, tmp, index, len);
            System.arraycopy(array, index, tmp, index + len, size - index);
            array = tmp;
        } else {
            System.arraycopy(array, index, array, len + index, size - index);
            System.arraycopy(bytes, start, array, index, len);
        }
        size += len;
        return size - 1;
    }


    /**
     * 在指定位置设置
     */
    public void set(int index, int b) {
        rangeCheck(index);
        array[index] = (byte) b;
    }

    /**
     * 值替换
     */
    public void replace(int index, byte[] bytes, int start, int len) {
        rangeCheckForReplace(index, bytes, start, len);
        System.arraycopy(bytes, start, array, index, len);
    }

    /**
     * 删除指定位置的元素
     */
    public byte remove(int index) {
        if (index == size - 1) {
            return array[--size];
        }
        rangeCheck(index);
        byte data = array[index];
        System.arraycopy(array, index + 1, array, index, size - index - 1);
        size--;
        return data;
    }

    /**
     * 批量删除
     */
    public int remove(int start, int len) {
        rangeCheckForThis(start, len);
        System.arraycopy(array, start + len, array, start, size - start - len);
        size -= len;
        return len;
    }


    /**
     * 清除数组
     */
    public int clear() {
        int s = size;
        size = 0;
        return s;
    }

    /**
     * 转为byte数组
     */
    public byte[] toByte() {
        return Arrays.copyOf(array, size);
    }

    public byte[] toByte(int start, int len) {
        rangeCheckForThis(start, len);

        byte[] bytes = new byte[len];
        System.arraycopy(array, start, bytes, 0, len);
        return bytes;
    }

    /**
     * 转为byte集合
     */
    public List<Byte> toByteList() {
        List<Byte> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(array[i]);
        }
        return list;
    }

    /**
     * 释放array多余空间
     *
     * @return 释放的大小(byte)
     */
    public int release() {
        if (size == array.length) {
            return 0;
        }
        int len = array.length;
        array = toByte();
        return len - size;

    }

    /**
     * 扩容
     */
    private void resize() {
        if (array.length == 0) {
            array = new byte[DEFAULT_INIT_SIZE];
        }
        if (size >= array.length) {
            byte[] tmp = new byte[(int) (size * (size > 4 ? 1.5 : 2))];
            System.arraycopy(array, 0, tmp, 0, size);
            array = tmp;
        }
    }

    @Override
    public void forEach(Consumer<? super Byte> action) {
        for (int i = 0; i < size; i++) {
            action.accept(array[i]);
        }
    }

    @Override
    public Spliterator<Byte> spliterator() {
        return new ByteSpliterator(array, 0, size);
    }

    static class ByteSpliterator implements Spliterator<Byte> {

        private byte[] array;
        private int start;
        private int end;

        public ByteSpliterator(byte[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Byte> action) {
            action.accept(array[start++]);
            return start < end;
        }

        @Override
        public Spliterator<Byte> trySplit() {
            int mid = (start + end) >> 1, e = end;
            return mid >= end ? null : new ByteSpliterator(array, end = mid, e);
        }

        @Override
        public long estimateSize() {
            return end - start;
        }

        @Override
        public int characteristics() {
            return Spliterator.SIZED;
        }
    }

    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {
            int index = -1;

            public boolean hasNext() {
                return index < size - 1;
            }

            public Byte next() {
                if (++index >= size) {
                    throw new NoSuchElementException();
                }
                return array[index];
            }

            public void remove() {
                if (index < 0) {
                    throw new IllegalStateException();
                }
                ByteArrayList.this.remove(index--);
            }

            public void forEachRemaining(Consumer<? super Byte> action) {
                for (int i = 0; i < size; i++) {
                    action.accept(array[i]);
                }
            }
        };
    }

    @Override
    public int hashCode() {
        if (size == 0) {
            return 0;
        }
        int h = 0;
        for (int i = 0; i < size; i++) {
            h += 31 * h + array[i];
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ByteArrayList) {
            ByteArrayList list = (ByteArrayList) obj;
            if (size != list.size) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (array[i] != list.array[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder(size * 2 + 2);
        sb.append("[");
        for (int i = 0; i < size; i++) {
            sb.append(array[i]).append(i + 1 == size ? "]" : ",");
        }
        return sb.toString();
    }

    @Override
    public ByteArrayList clone() {
        try {
            ByteArrayList clone = (ByteArrayList) super.clone();
            clone.array = array.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 范围检查
     */
    private void rangeCheck(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("size out of range,size:" + index + ",size:" + size);
        }
    }

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("size out of range,size:" + index + ",size:" + size);
        }
    }

    private void rangeCheckForBytes(byte[] bytes, int start, int len) {
        if (len < 0) {
            throw new IllegalArgumentException("copy len can not < 0");
        }
        if (start < 0 || start >= bytes.length || start + len > bytes.length) {
            throw new IndexOutOfBoundsException("size out of range," +
                    "start:" + start + "len:" + len + ",size:" + bytes.length);
        }
    }

    private void rangeCheckForThis(int start, int len) {
        if (len < 0) {
            throw new IllegalArgumentException("copy len can not < 0");
        }
        if (start < 0 || start >= size || start + len > size) {
            throw new IndexOutOfBoundsException("size out of range," +
                    "start:" + start + "len:" + len + ",size:" + size);
        }
    }

    private void rangeCheckForReplace(int index, byte[] bytes, int start, int len) {
        rangeCheck(index);
        rangeCheckForBytes(bytes, start, len);
        if (index + len > size) {
            throw new IndexOutOfBoundsException("size out of range,size:" + (index + len) + ",size:" + size);
        }
    }
}


