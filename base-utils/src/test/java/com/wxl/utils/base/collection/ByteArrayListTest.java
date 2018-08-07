package com.wxl.utils.base.collection;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static org.junit.Assert.*;


/**
 * Create by wuxingle on 2018/4/5
 * ByteArrayList测试
 */
public class ByteArrayListTest {

    @Test
    public void contains() {
        ByteArrayList list = new ByteArrayList("0123456789!0123456789".getBytes());
        assertTrue(list.indexOf("!4567!".getBytes(), 1, 4) == 4);
        assertTrue(list.lastIndexOf("!0123456!".getBytes(), 1, 7) == 11);
        assertTrue(list.contains("9!0".getBytes()));
    }


    @Test
    public void add() {
        ByteArrayList list = new ByteArrayList(5);
        list.add(12);
        list.addAll("abc".getBytes());
        list.addAll("abcdef".getBytes(), 3, 3);

        assertArrayEquals(list.toByte(), new byte[]{12, 'a', 'b', 'c', 'd', 'e', 'f'});

        list.add(1, 13);
        list.add(0, 11);
        list.add(list.size(), 20);
        assertArrayEquals(list.toByte(), new byte[]{11, 12, 13, 'a', 'b', 'c', 'd', 'e', 'f', 20});

        list.addAll(3, "ghi".getBytes());
        assertArrayEquals(list.toByte(),
                new byte[]{11, 12, 13, 'g', 'h', 'i', 'a', 'b', 'c', 'd', 'e', 'f', 20});

        list.addAll(list.size(), "abcdef".getBytes(), 3, 3);
        assertArrayEquals(list.toByte(),
                new byte[]{11, 12, 13, 'g', 'h', 'i', 'a', 'b', 'c', 'd', 'e', 'f', 20, 'd', 'e', 'f'});

        list.set(5, 22);
        assertArrayEquals(list.toByte(),
                new byte[]{11, 12, 13, 'g', 'h', 22, 'a', 'b', 'c', 'd', 'e', 'f', 20, 'd', 'e', 'f'});
    }

    @Test
    public void replace() {
        ByteArrayList list = new ByteArrayList("abcdefghijk".getBytes());

        list.replace(3, "xyz".getBytes(), 0, 3);
        assertArrayEquals(list.toByte(),
                new byte[]{'a', 'b', 'c', 'x', 'y', 'z', 'g', 'h', 'i', 'j', 'k'});
    }

    @Test
    public void remove() {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());
        list.remove(0);
        list.remove(list.size() - 1);
        list.remove(1);
        assertArrayEquals(list.toByte(),
                new byte[]{'b', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm'});

        list.remove(3, 6);
        assertArrayEquals(list.toByte(),
                new byte[]{'b', 'd', 'e', 'l', 'm'});
    }

    @Test
    public void clear() {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());

        list.clear();
        assertArrayEquals(list.toByte(), new byte[]{});
    }

    @Test
    public void toByte() {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());

        byte[] bs = list.toByte();
        assertArrayEquals(bs,
                new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n'});

        byte[] bs2 = list.toByte(3, 6);
        assertArrayEquals(bs2,
                new byte[]{'d', 'e', 'f', 'g', 'h', 'i'});
    }


    @Test
    public void release() {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());
        assertEquals(list.release(), 0);

        list.add(11);
        assertEquals(list.release(), 6);
    }

    @Test
    public void forEach() {
        byte[] bytes = "abcdefghijklmn".getBytes();
        ByteArrayList list = new ByteArrayList(bytes);
        list.forEach(new Consumer<Byte>() {
            byte i = 'a';

            @Override
            public void accept(Byte aByte) {
                assertEquals(aByte.byteValue(), i++);
            }
        });
    }

    @Test
    public void iterator() {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : list) {
            sb.append(b).append(",");
        }
        assertEquals(sb.toString(), "97,98,99,100,101,102,103,104,105,106,107,108,109,110,");

        Iterator<Byte> iterator = list.iterator();
        while (iterator.hasNext()) {
            Byte next = iterator.next();
            if (next > 'd' && next < 'i') {
                iterator.remove();
            }
        }
        assertArrayEquals(list.toByte(), "abcdijklmn".getBytes());
    }

    @Test
    public void testSplit() {
        ByteArrayList list = new ByteArrayList(
                "abcdefghijklmnabcdefghijklmn".getBytes());
        Spliterator<Byte> s1 = list.spliterator();
        Spliterator<Byte> s2 = s1.trySplit();
        StringBuilder sb1 = new StringBuilder();
        s1.forEachRemaining(sb1::append);
        StringBuilder sb2 = new StringBuilder();
        s2.forEachRemaining(sb2::append);
        assertEquals("", sb1.toString(), sb2.toString());
    }

    @Test
    public void testClone() throws Exception {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());
        ByteArrayList clone = list.clone();
        assertEquals(list, clone);

        Field field = ByteArrayList.class.getDeclaredField("array");
        field.setAccessible(true);
        Object arr1 = field.get(list);
        Object arr2 = field.get(clone);

        assertNotEquals(arr1, arr2);
        assertArrayEquals((byte[]) arr1, (byte[]) arr2);
    }
}