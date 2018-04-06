package com.wxl.utils.lock.redis;

import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Created by wuxingle on 2018/03/12
 * redis操作
 */
public class RedisSupport {

    //整数成功回复
    public static final Long NUMBER_RESULT_SUCCESS = 1L;
    //状态成功回复
    public static final String STATUS_RESULT_SUCCESS = "OK";

    //持久化的key
    public static final Long  KEY_PERSISTENT_RESULT = -1L;
    //key不存在
    public static final Long KEY_NOT_EXISTS_RESULT = -2L;


    private static final String DEL_WITH_VALUE_EQUALS_SCRPIT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private Pool<Jedis> pool;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public RedisSupport(Pool<Jedis> pool) {
        Assert.notNull(pool, "pool can not null");
        this.pool = pool;
    }

    /**
     * 订阅监听
     */
    public interface SubListener {
        /**
         * @return true 继续监听
         * false 取消监听
         */
        boolean onMessage(String channel, String message);
    }

    /**
     * 取消订阅类
     */
    public static class JedisUnsuber {

        private final JedisPubSub jedisPubSub;

        public JedisUnsuber(JedisPubSub jedisPubSub) {
            this.jedisPubSub = jedisPubSub;
        }

        public void unsubscribe() {
            jedisPubSub.unsubscribe();
        }

        public void unsubscribe(String... channels) {
            jedisPubSub.unsubscribe(channels);
        }
    }


    /**
     * redis操作
     */
    public <T> T doWithJedis(Function<Jedis, T> operation) throws JedisException {
        try (Jedis jedis = pool.getResource()) {
            return operation.apply(jedis);
        }
    }


    /**
     * 获取值
     */
    public String get(final String key) throws JedisException {
        return doWithJedis(jedis -> jedis.get(key));
    }

    public byte[] get(final byte[] key) throws JedisException {
        return doWithJedis(jedis -> jedis.get(key));
    }

    /**
     * 设置key,value
     */
    public boolean set(final String key, final String value) throws JedisException {
        return doWithJedis(jedis -> STATUS_RESULT_SUCCESS.equals(jedis.set(key, value)));
    }

    public boolean set(final byte[] key, byte[] value) throws JedisException {
        return doWithJedis(jedis -> STATUS_RESULT_SUCCESS.equals(jedis.set(key, value)));
    }

    /**
     * 设置key,value,过期时间ms
     */
    public boolean set(final String key, final String value, final int ms) throws JedisException {
        return doWithJedis(jedis -> STATUS_RESULT_SUCCESS.equals(jedis.psetex(key, (long) ms, value)));
    }

    public boolean set(final byte[] key, final byte[] value, final int ms) throws JedisException {
        return doWithJedis(jedis -> STATUS_RESULT_SUCCESS.equals(jedis.psetex(key, (long) ms, value)));
    }

    /**
     * 不存在时进行set
     */
    public boolean setIfAbsent(final String key, final String value) throws JedisException {
        return doWithJedis(jedis -> NUMBER_RESULT_SUCCESS.equals(jedis.setnx(key, value)));
    }

    public boolean setIfAbsent(final byte[] key, final byte[] value) throws JedisException {
        return doWithJedis(jedis -> NUMBER_RESULT_SUCCESS.equals(jedis.setnx(key, value)));
    }

    /**
     * 不存在时进行set
     * key,value,过期时间
     */
    public boolean setIfAbsent(final String key, final String value, final int ms) throws JedisException {
        return doWithJedis(jedis -> STATUS_RESULT_SUCCESS.equals(jedis.set(key, value, "NX", "PX", ms)));
    }

    public boolean setIfAbsent(final byte[] key, final byte[] value, final int ms) throws JedisException {
        return doWithJedis(jedis -> STATUS_RESULT_SUCCESS.equals(jedis.set(key, value, "NX".getBytes(), "PX".getBytes(), ms)));
    }

    /**
     * 删除一个key
     */
    public boolean del(final String key) throws JedisException {
        return doWithJedis(jedis -> NUMBER_RESULT_SUCCESS.equals(jedis.del(key)));
    }

    public boolean del(final byte[] key) throws JedisException {
        return doWithJedis(jedis -> NUMBER_RESULT_SUCCESS.equals(jedis.del(key)));
    }

    /**
     * key和value都相等则删除
     */
    public boolean del(final String key, final String value) throws JedisException {
        return doWithJedis(jedis -> NUMBER_RESULT_SUCCESS.equals(jedis.eval(DEL_WITH_VALUE_EQUALS_SCRPIT,
                Collections.singletonList(key), Collections.singletonList(value))));
    }

    public boolean del(final byte[] key, final byte[] value) throws JedisException {
        return doWithJedis(jedis -> NUMBER_RESULT_SUCCESS.equals(jedis.eval(DEL_WITH_VALUE_EQUALS_SCRPIT.getBytes(),
                Collections.singletonList(key), Collections.singletonList(value))));
    }

    /**
     * 删除多个key
     */
    public Long delWithPre(String key) throws JedisException {
        return delWithPattern(key + "*");
    }

    public Long delWithSuf(String key) throws JedisException {
        return delWithPattern("*" + key);
    }

    public Long delWithContains(String key) throws JedisException {
        return delWithPattern("*" + key + "*");
    }

    private Long delWithPattern(final String pattern) throws JedisException {
        return doWithJedis(jedis -> {
            Set<String> keys = jedis.keys(pattern);
            return jedis.del(keys.toArray(new String[keys.size()]));
        });
    }

    /**
     * 列出多个key
     *
     * @param pattern 支持通配符
     */
    public Set<String> keys(final String pattern) throws JedisException {
        return doWithJedis(jedis -> jedis.keys(pattern));
    }

    public Set<byte[]> keys(final byte[] pattern) throws JedisException {
        return doWithJedis(jedis -> jedis.keys(pattern));
    }

    /**
     * 返回剩余时间
     * 秒
     */
    public Long ttl(final String key){
        return doWithJedis(jedis -> jedis.ttl(key));
    }

    public Long ttl(final byte[] key){
        return doWithJedis(jedis -> jedis.ttl(key));
    }

    /**
     * 返回剩余时间
     * 毫秒
     */
    public Long pttl(final String key){
        return doWithJedis(jedis -> jedis.pttl(key));
    }

    public Long pttl(final byte[] key){
        return doWithJedis(jedis -> jedis.pttl(key));
    }


    /**
     * 同步订阅
     */
    public void subSync(final SubListener subListener, final String... channels) throws JedisException {
        doWithJedis(jedis -> {
            jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    //返回false取消订阅
                    if (!subListener.onMessage(channel, message)) {
                        unsubscribe();
                    }
                }
            }, channels);
            return null;
        });
    }

    /**
     * 异步订阅
     */
    public JedisUnsuber subAsync(final SubListener subListener, final String... channels) throws JedisException {
        final JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                //返回false取消订阅
                if (!subListener.onMessage(channel, message)) {
                    unsubscribe();
                }
            }
        };
        executorService.execute(() -> doWithJedis(jedis -> {
            jedis.subscribe(jedisPubSub, channels);
            return null;
        }));
        return new JedisUnsuber(jedisPubSub);
    }

    /**
     * 发布
     */
    public Long publish(final String channel, final String message) throws JedisException {
        return doWithJedis(jedis -> jedis.publish(channel, message));
    }

    public Long publish(final byte[] channel, byte[] message) throws JedisException {
        return doWithJedis(jedis -> jedis.publish(channel, message));
    }


}


