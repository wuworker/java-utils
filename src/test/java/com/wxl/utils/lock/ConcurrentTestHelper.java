package com.wxl.utils.lock;

import com.wxl.utils.ThreadUtils;
import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

/**
 * Create by wuxingle on 2018/4/7
 * 并发测试工具类
 */
public class ConcurrentTestHelper {


    public static void testConcurrent(Lock[] locks, int threadNum) {
        ExecutorService service = Executors.newFixedThreadPool(threadNum);
        Num safe = new Num(0);
        Num unsafe = new Num(0);

        CountDownLatch startLaunch = new CountDownLatch(threadNum);
        CountDownLatch endLaunch = new CountDownLatch(threadNum);

        for (int i = 0; i < threadNum; i++) {
            service.execute(new ConcurrentTask(
                    startLaunch, endLaunch, locks[threadNum % locks.length], safe, unsafe));
            startLaunch.countDown();
        }

        try {
            endLaunch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("all thread exec end,safe=" + safe + ",unsafe=" + unsafe);

        service.shutdown();
    }


    private static class Num {
        private int i;

        public Num(int i) {
            this.i = i;
        }

        public void add() {
            int n = i + 1;
            ThreadUtils.sleep(1);
            i = n;
        }

        @Override
        public String toString() {
            return "Num{" +
                    "i=" + i +
                    '}';
        }
    }

    private static class ConcurrentTask implements Runnable {

        private CountDownLatch startLaunch;

        private CountDownLatch endLaunch;

        private Lock lock;

        private Num safe;

        private Num unsafe;

        public ConcurrentTask(CountDownLatch startLaunch, CountDownLatch endLaunch, Lock lock, Num safe, Num unsafe) {
            this.startLaunch = startLaunch;
            this.endLaunch = endLaunch;
            this.lock = lock;
            this.safe = safe;
            this.unsafe = unsafe;
        }

        @Override
        public void run() {
            try {
                startLaunch.await();
                for (int i = 0; i < 10; i++) {
                    addUnsafe();
                    addSafe();
                }

                endLaunch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void addSafe() {
            try {
                lock.lock();
                safe.add();
            } finally {
                lock.unlock();
            }
        }

        private void addUnsafe() {
            unsafe.add();
        }
    }

}
