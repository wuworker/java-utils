package com.wxl.utils.base.collection;

import com.wxl.utils.base.ThreadUtils;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.SerializationUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by wuxingle on 2018/1/7 0007.
 * LazyCacheMapTest
 */
public class LazyCacheMapTest {

    /**
     * 打印cacheMap状态
     */
    private static void printLazyCacheMapField(LazyCacheMap lazyCacheMap, boolean printSelf) {
        Field table = ReflectionUtils.findField(LazyCacheMap.class, "table");
        table.setAccessible(true);
        Field size = ReflectionUtils.findField(LazyCacheMap.class, "size");
        size.setAccessible(true);
        Field modCount = ReflectionUtils.findField(LazyCacheMap.class, "modCount");
        modCount.setAccessible(true);
        Field threshold = ReflectionUtils.findField(LazyCacheMap.class, "threshold");
        threshold.setAccessible(true);
        Field loadFactor = ReflectionUtils.findField(LazyCacheMap.class, "loadFactor");
        loadFactor.setAccessible(true);

        Object[] tableArray = (Object[]) ReflectionUtils.getField(table, lazyCacheMap);

        System.out.println("start print -----------------------------------------------");
        if(printSelf)
            System.out.println("cacheMap: "+lazyCacheMap);
        System.out.println("table size: " + tableArray.length);
        System.out.println("table: " + Arrays.toString(tableArray));
        System.out.println("size: " + ReflectionUtils.getField(size, lazyCacheMap));
        System.out.println("modCount: " + ReflectionUtils.getField(modCount, lazyCacheMap));
        System.out.println("threshold: " + ReflectionUtils.getField(threshold, lazyCacheMap));
        System.out.println("loadFactor: " + ReflectionUtils.getField(loadFactor, lazyCacheMap));
        System.out.println("end print -----------------------------------------------");
    }


    @Test
    public void setExpire() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>();
        cacheMap.put("abc", 123, 3000);
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);
        System.out.println("ttl:"+cacheMap.ttl("abc"));

        cacheMap.setExpire("abc", 3000);
        ThreadUtils.sleep(3000);

        System.out.println(cacheMap.containsKey("abc"));
        printLazyCacheMapField(cacheMap,true);
    }

    @Test
    public void setPersistent() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>();
        cacheMap.put("abc", 123, 3000);
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);
        System.out.println("ttl:"+cacheMap.ttl("abc"));

        cacheMap.setPersistent("abc");
        ThreadUtils.sleep(3000);

        System.out.println(cacheMap.containsKey("abc"));
        printLazyCacheMapField(cacheMap,true);

    }

    @Test
    public void ttl() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>();
        cacheMap.put("abc", 123, 3000);
        cacheMap.put("def", 456);
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);
        System.out.println("ttl abc:"+cacheMap.ttl("abc"));
        System.out.println("ttl def:"+cacheMap.ttl("def"));
        System.out.println("ttl null:"+cacheMap.ttl("poi"));

        ThreadUtils.sleep(3000);

        printLazyCacheMapField(cacheMap,true);
    }

    @Test
    public void isPersistent() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>();
        cacheMap.put("abc", 123, 3000);
        cacheMap.put("def", 456);
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);
        System.out.println("persistent abc:"+cacheMap.isPersistent("abc"));
        System.out.println("persistent def:"+cacheMap.isPersistent("def"));

        ThreadUtils.sleep(3000);

        System.out.println("persistent abc:"+cacheMap.isPersistent("abc"));
        System.out.println("persistent def:"+cacheMap.isPersistent("def"));

        printLazyCacheMapField(cacheMap,true);
    }

    @Test
    public void put() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4);
        cacheMap.put("abc", 123, 3000);
        cacheMap.put("def", 456);
        printLazyCacheMapField(cacheMap,true);

        cacheMap.put("efg", 123, 3000);
        cacheMap.put("hji", 456);
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(3000);

        printLazyCacheMapField(cacheMap,true);
    }

    @Test
    public void putIfAbsent() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4);
        cacheMap.put("abc", 123, 2000);
        cacheMap.putIfAbsent("abc", "replace");
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(2000);

        cacheMap.putIfAbsent("abc", "replace");
        printLazyCacheMapField(cacheMap,true);
    }

    @Test
    public void size() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(8);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("jkl", 999);
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);
        printLazyCacheMapField(cacheMap,true);
    }

    @Test
    public void get() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        System.out.println(cacheMap.get("abc"));
        System.out.println(cacheMap.get("def"));
        System.out.println(cacheMap.get("ghi"));
        System.out.println(cacheMap.get("mno"));
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);
        System.out.println(cacheMap.get("abc"));
        System.out.println(cacheMap.get("def"));
        System.out.println(cacheMap.get("ghi"));
        System.out.println(cacheMap.get("mno"));
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);
        System.out.println(cacheMap.get("abc"));
        System.out.println(cacheMap.get("def"));
        System.out.println(cacheMap.get("ghi"));
        System.out.println(cacheMap.get("mno"));
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);
        System.out.println(cacheMap.get("abc"));
        System.out.println(cacheMap.get("def"));
        System.out.println(cacheMap.get("ghi"));
        System.out.println(cacheMap.get("mno"));
        printLazyCacheMapField(cacheMap,true);
    }

    @Test
    public void putAll() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);

        Map<String, Object> map = new HashMap<>(4,1);
        map.put("123", "789");
        map.put("345", 123);
        map.put("678", 998);

        LazyCacheMap<String,Object> cacheMap2 = new LazyCacheMap<>(cacheMap);
        printLazyCacheMapField(cacheMap2,true);

        cacheMap2.putAll(map);
        printLazyCacheMapField(cacheMap2,true);

        ThreadUtils.sleep(3000);
        printLazyCacheMapField(cacheMap2,true);

    }

    @Test
    public void remove() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);

        Object def = cacheMap.remove("def");
        System.out.println(def);
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);

        Object abc = cacheMap.remove("abc");
        System.out.println(abc);
        printLazyCacheMapField(cacheMap,true);

        boolean ghi = cacheMap.remove("ghi", 998);
        System.out.println(ghi);
        printLazyCacheMapField(cacheMap,true);

        boolean mon = cacheMap.remove("mno", 998);
        System.out.println(mon);
        printLazyCacheMapField(cacheMap,true);
    }

    @Test
    public void clear() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);

        cacheMap.clear();
        printLazyCacheMapField(cacheMap,true);
    }

    @Test
    public void containsValue() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("mno", 998);

        printLazyCacheMapField(cacheMap,true);

        boolean b = cacheMap.containsValue("789");
        System.out.println(b);

        ThreadUtils.sleep(1000);

        boolean b2 = cacheMap.containsValue("789");
        boolean b3 = cacheMap.containsValue(998);
        System.out.println(b2);
        System.out.println(b3);

        printLazyCacheMapField(cacheMap,false);
        System.out.println(cacheMap);
    }

    @Test
    public void keySet() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);

        Set<String> strings = cacheMap.keySet();
        for(String key:strings){
            System.out.println(key);
        }
        System.out.println("-------------");

        ThreadUtils.sleep(2000);

        for(String key:strings){
            System.out.println(key);
        }
        System.out.println("-------------");

        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);

        Iterator<String> iterator = strings.iterator();
        while (iterator.hasNext()){
            String next = iterator.next();
            if(next.equals("def")){
                iterator.remove();
            }
        }
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);

        Spliterator<String> spliterator = strings.spliterator();
        spliterator.forEachRemaining(System.out::println);

        printLazyCacheMapField(cacheMap,false);
    }

    @Test
    public void values() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);


        Collection<Object> values = cacheMap.values();
        for(Object v:values){
            System.out.println(v);
        }
        System.out.println("-------------");

        ThreadUtils.sleep(2000);

        for(Object v:values){
            System.out.println(v);
        }
        System.out.println("-------------");

        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);

        Iterator<Object> iterator = values.iterator();
        while (iterator.hasNext()){
            Object next = iterator.next();
            if(next.equals(123)){
                iterator.remove();
            }
        }
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);

        Spliterator<Object> spliterator = values.spliterator();
        spliterator.forEachRemaining(System.out::println);

        printLazyCacheMapField(cacheMap,false);
    }

    @Test
    public void entrySet() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);

        Set<Map.Entry<String,Object>> entrys = cacheMap.entrySet();
        for(Map.Entry<String,Object> entry:entrys){
            System.out.println(entry);
        }
        System.out.println("-------------");

        ThreadUtils.sleep(2000);

        for(Map.Entry<String,Object> entry:entrys){
            System.out.println(entry);
        }
        System.out.println("-------------");

        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);

        Iterator<Map.Entry<String,Object>> iterator = entrys.iterator();
        while (iterator.hasNext()){
            Map.Entry<String,Object> next = iterator.next();
            if(next.getKey().equals("def")){
                iterator.remove();
            }
        }
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);

        Spliterator<Map.Entry<String,Object>> spliterator = entrys.spliterator();
        spliterator.forEachRemaining(System.out::println);

        printLazyCacheMapField(cacheMap,false);
    }

    @Test
    public void getOrDefault() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);

        Object orDefault = cacheMap.getOrDefault("abc", "default");
        System.out.println(orDefault);

        ThreadUtils.sleep(1000);

        orDefault = cacheMap.getOrDefault("abc", "default");
        System.out.println(orDefault);

        printLazyCacheMapField(cacheMap,false);
        System.out.println(cacheMap);
    }

    @Test
    public void replace() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);

        Object def = cacheMap.replace("def", 456);
        System.out.println(def);
        printLazyCacheMapField(cacheMap,true);

        Object def2 = cacheMap.replace("def", 123,789);
        System.out.println(def2);
        printLazyCacheMapField(cacheMap,true);

        Object def3 = cacheMap.replace("def", 456,789);
        System.out.println(def3);
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);

        boolean abc = cacheMap.replace("abc", "789",1000000);
        System.out.println(abc);
        printLazyCacheMapField(cacheMap,true);

        Object abc2 = cacheMap.replace("abc", 1000000);
        System.out.println(abc2);
        printLazyCacheMapField(cacheMap,true);
    }


    @Test
    public void computeIfAbsent() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);

        Object abc = cacheMap.computeIfAbsent("abc", (k) -> k + "00000000");
        System.out.println(abc);
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);

        Object abc2 = cacheMap.computeIfAbsent("abc", (k) -> k + "00000000");
        System.out.println(abc2);
        printLazyCacheMapField(cacheMap,true);
    }

    @Test
    public void computeIfPresent() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);

        Object abc = cacheMap.computeIfPresent("abc", (k,v) -> k + v);
        System.out.println(abc);
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(1000);

        Object abc2 = cacheMap.computeIfPresent("abc", (k,v) -> k + v + "000000");
        System.out.println(abc2);
        printLazyCacheMapField(cacheMap,true);

        Object mno = cacheMap.computeIfPresent("mno", (k,v) -> null);
        System.out.println(mno);
        printLazyCacheMapField(cacheMap,true);
    }

    @Test
    public void compute() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);

        Object abc = cacheMap.compute("abc", (k,v) -> k + v);
        System.out.println(abc);
        printLazyCacheMapField(cacheMap,true);

        Object lll = cacheMap.compute("lll", (k,v) -> k + v);
        System.out.println(lll);
        printLazyCacheMapField(cacheMap,true);

        Object def = cacheMap.compute("def", (k,v) -> null);
        System.out.println(def);
        printLazyCacheMapField(cacheMap,true);

        Object ggg = cacheMap.compute("ggg", (k,v) -> null);
        System.out.println(ggg);
        printLazyCacheMapField(cacheMap,true);

    }

    @Test
    public void merge() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);

        Object abc = cacheMap.merge("abc", "defaulttttttt",(v1,v2) -> ""+v1 + v2);
        System.out.println(abc);
        printLazyCacheMapField(cacheMap,true);

        Object lll = cacheMap.merge("lll", "defaulttttttt",(v1,v2) -> ""+v1 + v2);
        System.out.println(lll);
        printLazyCacheMapField(cacheMap,true);

        Object def = cacheMap.merge("def", "defaulttttttt",(v1,v2) -> null);
        System.out.println(def);
        printLazyCacheMapField(cacheMap,true);

        Object ggg = cacheMap.merge("ggg", "defaulttttttt",(v1,v2) -> null);
        System.out.println(ggg);
        printLazyCacheMapField(cacheMap,true);
    }

    @Test
    public void forEach() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);
        cacheMap.forEach((k,v)->System.out.println(k+"==>"+v));
        System.out.println("------------------------------------");

        ThreadUtils.sleep(2000);

        cacheMap.forEach((k,v)->System.out.println(k+"==>"+v));
        printLazyCacheMapField(cacheMap,false);
        System.out.println(cacheMap);
    }

    @Test
    public void replaceAll() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);

        cacheMap.replaceAll((k,v)->k+v);
        printLazyCacheMapField(cacheMap,true);

        ThreadUtils.sleep(2000);

        cacheMap.replaceAll((k,v)->k+v+"000000000000");
        printLazyCacheMapField(cacheMap,false);
        System.out.println(cacheMap);
    }


    @Test
    public void testClone() throws Exception {
        LazyCacheMap<String, Object> cacheMap = new LazyCacheMap<>(4,1);
        cacheMap.put("abc", "789",1000);
        cacheMap.put("def", 123, 2000);
        cacheMap.put("ghi", 999, 3000);
        cacheMap.put("mno", 998);
        printLazyCacheMapField(cacheMap,true);

        byte[] bytes = SerializationUtils.serialize(cacheMap);

        LazyCacheMap<String, Object> cloneMap =
                (LazyCacheMap<String, Object>)SerializationUtils.deserialize(bytes);
        printLazyCacheMapField(cloneMap,true);

        System.out.println(cloneMap.equals(cacheMap));
        System.out.println(cacheMap == cloneMap);
    }

}