package com.wxl.utils.redis;

import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Created by wuxingle on 2018/03/12
 * redis操作，只实现了string类型的操作
 * 使用jedis实现,支持单实例,或者主从结构
 * 不支持集群、和客户端分片,因为使用的是jedis类
 */
public class RedisSupport {

    //整数成功回复
    public static final Long NUMBER_RESULT_SUCCESS = 1L;
    //状态成功回复
    public static final String STATUS_RESULT_SUCCESS = "OK";

    //持久化的key
    public static final Long KEY_PERSISTENT = -1L;

    public static final Long KEY_NOT_EXIST = -2L;

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

        private JedisUnsuber(JedisPubSub jedisPubSub) {
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
     * 执行lua脚本
     */
    public Object eval(String script, List<String> keys, List<String> values) {
        return eval(script, keys, values, Object.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T eval(String script, List<String> keys, List<String> values, Class<T> clazz)
            throws JedisException {
        return doWithJedis(jedis -> (T) jedis.eval(script, keys, values));
    }

    public Object eval(String script, Integer keyNum, String... args) {
        return eval(script, Object.class, keyNum, args);
    }

    @SuppressWarnings("unchecked")
    public <T> T eval(String script, Class<T> clazz, Integer keyNum, String... args)
            throws JedisException {
        List<String> keys = new ArrayList<>(keyNum);
        List<String> values = new ArrayList<>(args.length - keyNum);
        for (int i = 0; i < args.length; i++) {
            if (i < keyNum) {
                keys.add(args[i]);
            } else {
                values.add(args[i]);
            }
        }
        return doWithJedis(jedis -> (T) jedis.eval(script, keys, values));
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
     * 删除多个key
     */
    public Long delWithPre(String key) throws JedisException {
        return delWithPattern(key + "*");
    }

    public Long delWithEnd(String key) throws JedisException {
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
    public Long ttl(final String key) {
        return doWithJedis(jedis -> jedis.ttl(key));
    }

    public Long ttl(final byte[] key) {
        return doWithJedis(jedis -> jedis.ttl(key));
    }

    /**
     * 返回剩余时间
     * 毫秒
     */
    public Long pttl(final String key) {
        return doWithJedis(jedis -> jedis.pttl(key));
    }

    public Long pttl(final byte[] key) {
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


