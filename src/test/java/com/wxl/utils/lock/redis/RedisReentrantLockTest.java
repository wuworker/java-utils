package com.wxl.utils.lock.redis;

import com.wxl.utils.ThreadUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.util.Pool;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wuxingle on 2018/03/12
 */
public class RedisReentrantLockTest {

    private static Pool<Jedis> pool;

    @BeforeClass
    public static void init() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(100);
        poolConfig.setMaxIdle(5);
        poolConfig.setMaxWaitMillis(5000);
        poolConfig.setTestOnBorrow(true);

        pool = new JedisPool(poolConfig, "localhost", 6379, 5000);

    }

    @AfterClass
    public static void after() {
        pool.destroy();
    }

    @Test
    public void testSubPub(){
        try (Jedis jedis = pool.getResource()){
            new Thread(){
                @Override
                public void run() {
                    try {
                        System.out.println("thread start");
                        Thread.sleep(10000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    jedis.getClient().unsubscribe("test");
                    System.out.println("thread end");
                }
            }.start();
            jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    System.out.println("on message,on channel:"+channel+",message:"+message);
                }

                @Override
                public void onSubscribe(String channel, int subscribedChannels) {
                    System.out.println("on sub, on channle:"+channel+",channel:"+subscribedChannels);
                }

                @Override
                public void onUnsubscribe(String channel, int subscribedChannels) {
                    System.out.println("on unsub, on channle:"+channel+",channel:"+subscribedChannels);
                }
            },"test");
        }
    }

    @Test
    public void test() throws Exception {
        RedisReentrantLock lock = new RedisReentrantLock(pool, "testLock", 10);
        lock.lock();

        Thread.sleep(5000);

        lock.unlock();
    }


    @Test
    public void testLock() throws Exception {

        class Num {
            int num;
        }

        class LockTask implements Runnable {
            private RedisReentrantLock lock;
            private CountDownLatch startLatch;
            private CountDownLatch endLatch;
            private Num safeNum;
            private Num unsafeNum;
            private Random random = new Random();

            public LockTask(RedisReentrantLock lock, CountDownLatch startLatch, CountDownLatch endLatch, Num safeNum, Num unsafeNum) {
                this.lock = lock;
                this.startLatch = startLatch;
                this.endLatch = endLatch;
                this.safeNum = safeNum;
                this.unsafeNum = unsafeNum;
            }

            public void run() {
                try {
                    startLatch.await();

                    addNum(unsafeNum);

                    lock.lock();
                    addNum(safeNum);
                    lock.unlock();

                    endLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void addNum(Num num) {
                int n = num.num;
                n++;
                ThreadUtils.sleep(random.nextInt(1));
                num.num = n;
            }
        }

        Num safe = new Num();
        Num unsafe = new Num();
        int taskNum = 100;
        int zkLockNum = 10;
        RedisReentrantLock[] redisReentrantLocks = new RedisReentrantLock[zkLockNum];
        LockTask[] lockTasks = new LockTask[taskNum];
        CountDownLatch startLatch = new CountDownLatch(taskNum);
        CountDownLatch endLatch = new CountDownLatch(taskNum);

        for (int i = 0; i < lockTasks.length; i++) {
            RedisReentrantLock lock;
            if ((lock = redisReentrantLocks[i % zkLockNum]) == null) {
                lock = redisReentrantLocks[i] = new RedisReentrantLock(pool, "testLock", 10);
            }
            lockTasks[i] = new LockTask(lock, startLatch, endLatch, safe, unsafe);
        }

        ExecutorService service = Executors.newFixedThreadPool(taskNum);
        for (LockTask t1 : lockTasks) {
            service.execute(t1);
            startLatch.countDown();
        }

        endLatch.await();
        service.shutdown();

        System.out.println(safe.num);
        System.out.println(unsafe.num);
    }


}