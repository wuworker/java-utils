package com.wxl.utils.lock.redis;

import com.wxl.utils.lock.ConcurrentTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

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
    public void test() throws Exception {
        RedisReentrantLock lock = new RedisReentrantLock(
                pool, "testLock", 10000);
        lock.lock();

        Thread.sleep(5000);

        lock.unlock();
    }


    @Test
    public void testLock() throws Exception {
        RedisReentrantLock[] locks = new RedisReentrantLock[10];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new RedisReentrantLock(pool,"testLock",10000);
        }
        ConcurrentTestHelper.testConcurrent(locks,100);
    }


}