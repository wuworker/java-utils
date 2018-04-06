package com.wxl.utils.map;

import com.wxl.utils.RandomUtils;
import com.wxl.utils.collection.TimerClearMap;
import org.junit.Test;

import java.util.Random;

/**
 * Created by wuxingle on 2018/1/4 0004.
 * TimerClearMapTest
 */
public class TimerClearMapTest {

    @Test
    public void test() {
        TimerClearMap<String, String> clearMap1 =
                new TimerClearMap<>(10);
        TimerClearMap<String, String> clearMap2 =
                new TimerClearMap<>(5);
        TimerClearMap<String, String> clearMap3 =
                new TimerClearMap<>(15);

        Random random = new Random(10);
        for (int i = 0; i < 5; i++) {
            new Thread() {
                @Override
                public void run() {
                    TimerClearMapTest.this.sleep(random.nextInt(1000));
                    for (int i = 0; i < 5; i++) {
                        String key = RandomUtils.generateAbcNum(random, 6);
                        String value = RandomUtils.generateAbcNum(random, 6);
                        clearMap1.put(key, value);
                        clearMap2.put(key, value);
                        clearMap3.put(key, value);
                    }
                }
            }.start();
        }
        sleep(1000);
        System.out.println("1s");
        System.out.println(clearMap1);
        System.out.println(clearMap2);
        System.out.println(clearMap3);

        sleep(5000);
        System.out.println("6s");
        System.out.println(clearMap1);
        System.out.println(clearMap2);
        System.out.println(clearMap3);

        sleep(5000);
        System.out.println("11s");
        System.out.println(clearMap1);
        System.out.println(clearMap2);
        System.out.println(clearMap3);

        sleep(5000);
        System.out.println("16s");
        System.out.println(clearMap1);
        System.out.println(clearMap2);
        System.out.println(clearMap3);
    }


    private void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}