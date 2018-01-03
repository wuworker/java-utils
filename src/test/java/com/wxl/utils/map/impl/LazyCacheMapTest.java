package com.wxl.utils.map.impl;

import com.wxl.utils.map.CacheMap;
import com.wxl.utils.map.LazyCacheMap2;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.SerializationUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wuxingle on 2017/12/5.
 * LazyCacheMapTest
 */
public class LazyCacheMapTest {

    private static CacheMap<Integer,String> cacheMap = new LazyCacheMap2<>();

    @Test
    public void setExpire() throws Exception {
        cacheMap.put(1,"a");
        System.out.println(cacheMap);
        cacheMap.setExpire(1,500);
        System.out.println(cacheMap);

        sleep(200);
        System.out.println(cacheMap);
        Assert.assertTrue(cacheMap.size() == 1);

        cacheMap.setExpire(1,500);
        sleep(300);
        System.out.println(cacheMap);
        Assert.assertTrue(cacheMap.size() == 1);

        sleep(200);
        System.out.println(cacheMap);

        Assert.assertTrue(cacheMap.size() == 0);
    }

    @Test
    public void setPersistent() throws Exception {
        cacheMap.put(1,"a",500);
        sleep(200);
        System.out.println(cacheMap);

        cacheMap.setPersistent(1);
        sleep(400);
        System.out.println(cacheMap);
        Assert.assertTrue(cacheMap.size() == 1);
    }

    @Test
    public void ttl() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",1000);
        System.out.println(cacheMap.ttl(2));

        Assert.assertTrue(cacheMap.ttl(1) == CacheMap.PERSISTENT_KEY);
        Assert.assertTrue(cacheMap.ttl(3) == CacheMap.NOT_EXIST_KEY);

        sleep(200);
        System.out.println(cacheMap.ttl(2));

        sleep(500);
        System.out.println(cacheMap.ttl(2));

        sleep(400);
        System.out.println(cacheMap.ttl(2));
    }

    @Test
    public void isPersistent() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",1000);
        System.out.println(cacheMap.isPersistent(1));
        System.out.println(cacheMap.isPersistent(2));

        cacheMap.setExpire(1,1000);
        cacheMap.setPersistent(2);
        System.out.println(cacheMap.isPersistent(1));
        System.out.println(cacheMap.isPersistent(2));
    }


    @Test
    public void size() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);
        System.out.println(cacheMap.size());

        sleep(500);
        System.out.println(cacheMap.size());

        sleep(500);
        System.out.println(cacheMap.size());
    }

    @Test
    public void keySet() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        Set<Integer> keySet = cacheMap.keySet();
        System.out.println(keySet.containsAll(Arrays.asList(1,2)));
        System.out.println(keySet.contains(1));
        System.out.println(keySet.remove(1));
        System.out.println(cacheMap.get(1));
        System.out.println(keySet.removeAll(Arrays.asList(1,2,3)));
        System.out.println(keySet.size());

        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        System.out.println(keySet.retainAll(Arrays.asList(3,4,5)));
        System.out.println(keySet);

        keySet.clear();
        System.out.println(keySet.size());

        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);
        cacheMap.put(4,"d");
        cacheMap.put(5,"e",500);
        cacheMap.put(6,"f",1000);

        System.out.println(keySet);

        keySet.spliterator().forEachRemaining((e)->{
            if(e==1){
                sleep(500);
            }
            System.out.println(e+",");
        });

        Spliterator<Integer> spliterator = keySet.spliterator();
        Spliterator<Integer> spliterator2 = spliterator.trySplit();
        Spliterator<Integer> spliterator3 = spliterator2.trySplit();

        spliterator.forEachRemaining((t)->System.out.print(t+","));
        System.out.println();
        spliterator2.forEachRemaining((t)->System.out.print(t+","));
        System.out.println();
        spliterator3.forEachRemaining((t)->System.out.print(t+","));
        System.out.println();

        cacheMap.put(8,"sdf",2000);
        System.out.println(keySet);
        System.out.println(keySet.stream().filter((t)->{
            if(t == 1){
                sleep(1000);
            }
            return t%2==0;})
                .skip(1).collect(Collectors.toSet()));
    }

    @Test
    public void values() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        Collection<String> values = cacheMap.values();
        System.out.println(values.containsAll(Arrays.asList("a","b")));
        System.out.println(values.contains("c"));
        System.out.println(values.remove("c"));
        System.out.println(cacheMap.get(3));
        System.out.println(values.removeAll(Arrays.asList("a","b","c")));
        System.out.println(values.size());

        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        System.out.println(values.retainAll(Arrays.asList("b","c","e")));
        System.out.println(values);

        values.clear();
        System.out.println(values.size());

        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);
        cacheMap.put(4,"d");
        cacheMap.put(5,"e",500);
        cacheMap.put(6,"f",1000);

        System.out.println(values);

        values.spliterator().forEachRemaining((e)->{
            if("a".equals(e)){
                sleep(500);
            }
            System.out.println(e+",");
        });

        Spliterator<String> spliterator = values.spliterator();
        Spliterator<String> spliterator2 = spliterator.trySplit();
        Spliterator<String> spliterator3 = spliterator2.trySplit();

        spliterator.forEachRemaining((t)->System.out.print(t+","));
        System.out.println();
        spliterator2.forEachRemaining((t)->System.out.print(t+","));
        System.out.println();
        spliterator3.forEachRemaining((t)->System.out.print(t+","));
        System.out.println();

        cacheMap.put(8,"sdf",2000);
        System.out.println(values);

        values.stream().filter((t)->{
            if("a".equals(t)){
                sleep(1000);
            }
            return true;
        }).forEach(System.out::print);
    }

    @Test
    public void entrySet() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        //拿到的最后一个数据仍有可能是过期数据
        Iterator<Map.Entry<Integer, String>> iterator = cacheMap.entrySet().iterator();
        int i=0;
        while (iterator.hasNext()){
            Map.Entry<Integer, String> next = iterator.next();
            if(i<2){
                sleep(500);
            }
            System.out.println(next);
            i++;
        }
    }

    @Test
    public void containsValue() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        System.out.println(cacheMap.containsValue("b"));

        sleep(500);

        System.out.println(cacheMap.containsValue("b"));
    }

    @Test
    public void containsKey() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        System.out.println(cacheMap.containsKey(2));

        sleep(500);

        System.out.println(cacheMap.containsKey(2));
    }

    @Test
    public void get() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        System.out.println(cacheMap.get(2));
        System.out.println(cacheMap.get(3));

        sleep(500);

        System.out.println(cacheMap.get(2));
        System.out.println(cacheMap.get(3));
    }

    @Test
    public void remove() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        System.out.println(cacheMap.get(2));
        System.out.println(cacheMap.get(3));

        cacheMap.remove(2);

        System.out.println(cacheMap.get(2));
        System.out.println(cacheMap.get(3));
    }


    @Test
    public void testToString() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        System.out.println(cacheMap);

        sleep(500);

        System.out.println(cacheMap);
    }

    @Test
    public void forEach() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        cacheMap.forEach((k,v)->{
            if(k==1){
                sleep(500);
            }
            System.out.println(k+"==>"+v);
        });
    }

    @Test
    public void replaceAll() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        cacheMap.replaceAll((k,v)->{
            if(k==1){
                sleep(500);
            }
            return k+v;
        });

        System.out.println(cacheMap);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerializable(){
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        byte[] byts = SerializationUtils.serialize(cacheMap);
        sleep(500);
        CacheMap<Integer,String> cacheMap2 = (CacheMap<Integer,String>)SerializationUtils.deserialize(byts);
        System.out.println(cacheMap2);
        System.out.println(cacheMap.equals(cacheMap2));
    }


    @Test
    public void testClone() throws Exception {
        cacheMap.put(1,"a");
        cacheMap.put(2,"b",500);
        cacheMap.put(3,"c",1000);

        LazyCacheMap2<Integer, String> clone2 = ((LazyCacheMap2<Integer, String>) cacheMap).clone();
        System.out.println(clone2);

        System.out.println(cacheMap == clone2);
        System.out.println(cacheMap.equals(clone2));
    }


    @Test
    public void testStream(){
        Map<Integer,String> map = new HashMap<>();
        Random random = new Random(1);
        for(int i=0;i<100_000;i++){
            int num = random.nextInt();
            map.put(num,i+"");
            cacheMap.put(num,i+"",random.nextInt(1000)+500);
        }
        System.out.println(cacheMap.equals(map));

        long count1 = map.entrySet().parallelStream().filter((t) -> t.getKey() > 0).count();
        long count2 = cacheMap.entrySet().parallelStream().filter((t) -> t.getKey() > 0).count();

        System.out.println(count1);
        System.out.println(count2);

        sleep(750);

        long count3 = cacheMap.entrySet().parallelStream().filter((t) -> t.getKey() > 0).count();
        System.out.println(count3);
    }



    private void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}