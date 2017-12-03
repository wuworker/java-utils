package com.wxl.utils.map.impl;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.SerializationUtils;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Created by wuxingle on 2017/12/2 0002.
 * 懒加载缓存map测试
 */
public class LazyCacheMapTest {

    @Test
    public void testBase(){
        LazyCacheMap<Integer,String> cacheMap = new LazyCacheMap<>();

        cacheMap.put(1,"no expire");
        cacheMap.put(2,"expire",1000);
        Assert.assertTrue(cacheMap.isPersistent(1));
        Assert.assertTrue(cacheMap.size() == 2);
        System.out.println(cacheMap);

        sleep(1100);
        Assert.assertTrue(cacheMap.size() == 1);
        System.out.println(cacheMap);

        cacheMap.setExpire(1,1000);
        Assert.assertTrue(!cacheMap.isPersistent(1));
        sleep(1000);
        Assert.assertTrue(cacheMap.size() == 0);
        System.out.println(cacheMap);

        cacheMap.put(2,"haha2",1000);
        cacheMap.put(4,"haha4",2000);
        cacheMap.put(1,"haha1",3000);
        cacheMap.put(3,"haha3",4000);
        System.out.println(cacheMap);

        for(Map.Entry<Integer,String> entry:cacheMap.entrySet()){
            System.out.println(entry.getKey()+"==>"+entry.getValue());
        }

        sleep(2500);

        System.out.println("---------");
        for(Map.Entry<Integer,String> entry:cacheMap.entrySet()){
            System.out.println(entry.getKey()+"==>"+entry.getValue());
        }

        System.out.println(cacheMap.containsKey(1));
        System.out.println(cacheMap.containsValue("haha3"));
        System.out.println(cacheMap);

        cacheMap.remove(1);
        System.out.println(cacheMap);

        System.out.println(cacheMap.ttl(3));
        cacheMap.setPersistent(3);
        Assert.assertTrue(cacheMap.isPersistent(3));
        System.out.println(cacheMap);
    }


    /**
     * 当hasNext返回true时，next()拿到发现是过期数据，则自动获取下一个
     * 如果最后一个仍是过期,则以key和value都为null的对象代替.
     */
    @Test
    public void testLastExpire(){
        LazyCacheMap<Integer,String> cacheMap = new LazyCacheMap<>();

        cacheMap.put(1,"haha1",2000);
        cacheMap.put(2,"haha2",1000);
        cacheMap.put(3,"haha3",1000);
        System.out.println(cacheMap);

        Iterator<Map.Entry<Integer,String>> it = cacheMap.entrySet().iterator();
        int index = 0 ;
        while (it.hasNext()){
            if(index == 1){
                sleep(1500);
            }
            System.out.println(it.next());
            index++;
        }
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSync(){
        LazyCacheMap<Integer,String> map = new LazyCacheMap<>();
        Random r = new Random();
        for(int i=0;i<10;i++){
            map.put(r.nextInt(),"abc"+r.nextInt());
        }

        Iterator<Map.Entry<Integer,String>> it = map.entrySet().iterator();
        int i=0;
        while (it.hasNext()){
            if(i == 5){
                new Thread(()->{
                    map.put(0,"999");
                }).start();

                sleep(2000);
            }
            System.out.println(it.next());
            i++;
        }

        System.out.println("-------------");
        System.out.println(map);
    }


    @Test
    public void testClone(){
        LazyCacheMap<Integer,String> map = new LazyCacheMap<>();
        Random r = new Random();
        for(int i=0;i<5;i++){
            map.put(r.nextInt(),"abc"+r.nextInt(),r.nextInt(2000));
        }
        System.out.println(map);

        sleep(1000);

        LazyCacheMap<Integer,String> map2 = map.clone();
        System.out.println(map == map2);
        System.out.println(map.equals(map2));

        System.out.println(map);
        System.out.println(map2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerializable(){
        LazyCacheMap<Integer,String> map = new LazyCacheMap<>();
        Random r = new Random();
        for(int i=0;i<5;i++){
            map.put(r.nextInt(),"abc"+r.nextInt(),r.nextInt(2000));
        }
        System.out.println(map);

        sleep(1000);

        byte[] byts = SerializationUtils.serialize(map);
        LazyCacheMap<Integer,String> map2 = (LazyCacheMap<Integer,String>)SerializationUtils.deserialize(byts);

        System.out.println(map == map2);
        System.out.println(map.equals(map2));

        System.out.println(map);
        System.out.println(map2);
    }


    private void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}