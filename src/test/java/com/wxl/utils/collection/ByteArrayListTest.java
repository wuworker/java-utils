package com.wxl.utils.collection;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Create by wuxingle on 2018/4/5
 * ByteArrayList测试
 */
public class ByteArrayListTest {

    @Test
    public void contains(){
        ByteArrayList list = new ByteArrayList("0123456789!0123456789".getBytes());
        System.out.println(list);

        System.out.println(list.indexOf("!4567!".getBytes(),1,4));
        System.out.println(list.lastIndexOf("!0123456!".getBytes(),1,7));
        System.out.println(list.contains("9!0".getBytes()));
    }


    @Test
    public void add() {
        ByteArrayList list = new ByteArrayList(5);
        list.add(12);
        list.addAll("abc".getBytes());
        list.addAll("abcdef".getBytes(), 3, 3);
        System.out.println(list);
        System.out.println(list.size());
        list.add(1, 13);
        list.add(0, 11);
        list.add(list.size(), 20);
        System.out.println(list);

        list.addAll(3, "ghi".getBytes());
        System.out.println(list);

        list.addAll(list.size(), "abcdef".getBytes(), 3, 3);
        System.out.println(list);

        list.set(5, 22);
        System.out.println(list);
    }

    @Test
    public void replace() {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());
        System.out.println(list);

        list.replace(3, "xyz".getBytes(), 0, 3);
        System.out.println(list);
    }

    @Test
    public void remove() {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());
        System.out.println(list);
        list.remove(0);
        list.remove(list.size() - 1);
        list.remove(1);
        System.out.println(list);

        list.remove(3, 6);
        System.out.println(list);
    }

    @Test
    public void clear() {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());
        System.out.println(list);

        list.clear();
        System.out.println(list);
    }

    @Test
    public void toByte() {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());
        System.out.println(list);
        byte[] bs = list.toByte();
        System.out.println(Arrays.toString(bs));

        byte[] bs2 = list.toByte(3, 6);
        System.out.println(Arrays.toString(bs2));
    }


    @Test
    public void release() {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());
        System.out.println(list.size());
        System.out.println(list.release());

        list.add(11);
        System.out.println(list.size());
        System.out.println(list.release());
    }

    @Test
    public void forEach() {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());
        System.out.println(list);

        list.forEach((b) -> System.out.print(b + ","));
    }

    @Test
    public void iterator() {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());
        for(byte b : list){
            System.out.print(b+",");
        }
        System.out.println();

        Iterator<Byte> iterator = list.iterator();
        while (iterator.hasNext()){
            Byte next = iterator.next();
            if(next > 100 && next <105){
                iterator.remove();
            }
            System.out.print(next+",");
        }
        System.out.println();
        System.out.println(list.size());
        System.out.println(list);
    }

    @Test
    public void testSplit(){
        ByteArrayList list = new ByteArrayList("abcdefghijklmnabcdefghijklmn".getBytes());
        Spliterator<Byte> s1 = list.spliterator();
        Spliterator<Byte> s2 = s1.trySplit();
        s1.forEachRemaining((b)->System.out.print(b+","));
        System.out.println();
        s2.forEachRemaining((b)->System.out.print(b+","));
    }

    @Test
    public void testClone() throws Exception {
        ByteArrayList list = new ByteArrayList("abcdefghijklmn".getBytes());
        ByteArrayList clone = list.clone();
        System.out.println(list);
        System.out.println(clone);
        System.out.println(list.equals(clone));

        Field field = ByteArrayList.class.getDeclaredField("array");
        field.setAccessible(true);
        Object arr1 = field.get(list);
        Object arr2 = field.get(clone);

        System.out.println(arr1);
        System.out.println(arr2);
        System.out.println(arr1 == arr2);
    }
}