package com.wxl.utils.redis;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

/**
 * Created by wuxingle on 2018/03/12
 * redisSupport
 */
public class RedisSupportTest {

    private static Pool<Jedis> pool;

    private static RedisSupport redisSupport;

    @BeforeClass
    public static void init() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(5);
        poolConfig.setMaxIdle(1);
        poolConfig.setMaxWaitMillis(5000);
        poolConfig.setTestOnBorrow(true);

        pool = new JedisPool(poolConfig, "localhost", 6379, 5000);
        redisSupport = new RedisSupport(pool);
    }

    @AfterClass
    public static void after() {
        pool.destroy();
    }

    @Test
    public void get() throws Exception {
        redisSupport.set("haha", "123");
        System.out.println(redisSupport.get("haha"));
    }

    @Test
    public void setIfAbsent() throws Exception {
        redisSupport.set("haha", "123");
        System.out.println(redisSupport.setIfAbsent("haha", "lala", 5));

        redisSupport.del("haha");
        System.out.println(redisSupport.setIfAbsent("haha", "lala", 5));
        System.out.println(redisSupport.get("haha"));

        Thread.sleep(5000);
        System.out.println(redisSupport.get("haha"));

    }


    @Test
    public void delWithPre() throws Exception {
        redisSupport.set("haha1", "123");
        redisSupport.set("haha2", "123");
        redisSupport.set("haha3", "123");

        System.out.println(redisSupport.keys("haha*"));
        System.out.println(redisSupport.delWithPre("haha"));
        System.out.println(redisSupport.keys("haha*"));
    }


    @Test
    public void testSubPub() throws Exception {
        redisSupport.subSync((ch, msg) -> {
            System.out.println("sync once channel:" + ch + ",msg: " + msg);
            return false;
        }, "test");
        System.out.println("sync return");

        RedisSupport.JedisUnsuber unsuber = redisSupport.subAsync((ch, msg) -> {
            System.out.println("async once channel:" + ch + ",msg: " + msg);
            return true;
        }, "test");

        System.out.println("async return");

        Thread.sleep(10000);


        unsuber.unsubscribe();
        unsuber.unsubscribe();
        unsuber.unsubscribe();
        System.out.println("async un scribe return");

        Thread.sleep(10000);
    }

}


