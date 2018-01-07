package com.wxl.utils;

import org.junit.Test;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuxingle on 2017/12/17 0017.
 * 定时器测试
 */
public class TimerTest {

    /**
     * 第0秒，算出下一次第2秒执行，输出exec
     * 第2秒，算出下一次第4s执行，但是被sleep阻塞
     * 第5秒，sleep结束，输出exec，继续循环，发现已经是第5秒了，算出下一次第7秒执行，又输出exec
     * 第7秒，输出exec
     * 第9秒，输出exec
     */
    @Test
    public void testSchedule() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                if (i == 1) {
                    sleep(3000);
                }
                i++;
                System.out.println("exec:" + DateUtils.format(new Date(), "HH:mm:ss"));
            }
        }, 0L, 2000L);

        sleep(10000);
    }


    /**
     * 第0秒，算出下一次第2秒执行，输出exec
     * 第2秒，算出下一次第4s执行，但是被sleep阻塞
     * 第5秒，sleep结束，输出exec，继续循环，发现已经是第5秒了，算出下一次第6秒执行，又输出exec
     * 第6秒，输出exec
     * 第8秒，输出exec
     */
    @Test
    public void testScheduleAtFixRate() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                if (i == 1) {
                    sleep(3000);
                }
                i++;
                System.out.println("exec:" + DateUtils.format(new Date(), "HH:mm:ss"));
            }
        }, 0L, 2000L);

        sleep(10000);
    }


    @Test
    public void testScheduleExecutor(){
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(() -> {
            System.out.println("exe");
        }, 0L, 2, TimeUnit.SECONDS);

        sleep(4000);
        System.out.println("取消");
        future.cancel(true);
        sleep(4000);

        executorService.shutdown();
    }






    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}























