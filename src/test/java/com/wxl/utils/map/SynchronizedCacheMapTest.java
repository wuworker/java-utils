package com.wxl.utils.map;

import com.wxl.utils.collection.CacheMap;
import com.wxl.utils.collection.LazyCacheMap;
import com.wxl.utils.collection.SynchronizedCacheMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wuxingle on 2017/12/6.
 * SynchronizedCacheMapTest
 */
public class SynchronizedCacheMapTest {


    private static ExecutorService executorService = Executors.newFixedThreadPool(100);

    /**
     * 2个map的结果不同
     */
    @Test
    public void testUnSync() {
        Map<Integer, String> map = new HashMap<>();
        CacheMap<Integer, String> cacheMap = new LazyCacheMap<>();

        test1(map,cacheMap);

        System.out.println(map.equals(cacheMap));
        System.out.println(map.size());
        System.out.println(cacheMap.size());

    }

    /**
     * 2个map结果大小相同，但不一定每个元素相等
     */
    @Test
    public void testSync() {
        Map<Integer, String> map = Collections.synchronizedMap(new HashMap<>());
        CacheMap<Integer, String> cacheMap = new SynchronizedCacheMap<>();

        test1(map,cacheMap);

        System.out.println(map.equals(cacheMap));
        System.out.println(map.size());
        System.out.println(cacheMap.size());

        Assert.assertTrue(map.size() == cacheMap.size());

    }

    private void test1(Map<Integer, String> map, CacheMap<Integer, String> cacheMap){
        final Random random = new Random(1);
        final CountDownLatch countDownLatch = new CountDownLatch(100);
        final CountDownLatch countDownLatch2 = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            executorService.execute(() -> {
                countDownLatch.countDown();
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < 10; j++) {
                    int key = random.nextInt(1000);
                    String value = key + "|" + random.nextInt(100);
                    //可能2个线程生成了相同的key，不同的value.
                    //map先放入了线程1的value，然后切换到线程2执行完成，然后切换回来继续执行
                    //这样map里的value是线程2的,cacheMap里的value是线程1的,
                    //但是不管怎么样，他们大小一定相同
                    map.put(key, value);
                    cacheMap.put(key, value);
                }
                countDownLatch2.countDown();
            });
        }

        try {
            countDownLatch2.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executorService.shutdown();
    }

    @Test
    public void testUnSync2() {
        Map<Integer, String> map = new HashMap<>();
        CacheMap<Integer, String> cacheMap = new LazyCacheMap<>();
        List<Exception> errors = new ArrayList<>();
        test2(map, cacheMap,errors);


        System.out.println(errors.size());
    }

    @Test
    public void testSync2() {
        Map<Integer, String> map = Collections.synchronizedMap(new HashMap<>());
        CacheMap<Integer, String> cacheMap = new SynchronizedCacheMap<>();
        List<Exception> errors = new ArrayList<>();
        test2(map, cacheMap,errors);

        System.out.println(errors.size());
    }


    private void test2(Map<Integer, String> map, CacheMap<Integer, String> cacheMap,List<Exception> list) {
        final Random random = new Random(1);
        final CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 100; i++) {
            executorService.execute(() -> {
                countDownLatch.countDown();
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (int j = 0; j < 10; j++) {
                    try {
                        //put
                        if (random.nextBoolean()) {
                            int key = random.nextInt(1000);
                            String value = key + "," + random.nextInt(100);
                            map.put(key, value);
                            cacheMap.put(key, value);
                        }
                        //foreach
                        else {
                            map.toString();
                            cacheMap.toString();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        list.add(e);
                    }
                }
            });
        }


    }


}