package com.wxl.utils.lock.redis;

import com.wxl.utils.annotation.ThreadSafe;
import com.wxl.utils.lock.AbstractDistributeLock;
import com.wxl.utils.lock.DistributeLockException;
import com.wxl.utils.lock.DistributeSync;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by wuxingle on 2018/03/12
 * redis的分布式锁
 * 适用于单实例，不支持集群，客户端分片。
 * 存在的问题是
 * 如果是主从复制，当客户端A往master写入key时，master在复制到slaves之前宕机。
 * 那么slave升级为master后没有这个key的信息,也就是客户端B也可能拿到锁。
 * 同时要合理设置锁过期时间，保证业务执行时间小于锁过期时间
 */
@Slf4j
@ThreadSafe
public class RedisReentrantLock extends AbstractDistributeLock {

    //锁名字,同时也是sub/pub的channel
    private final String lockName;

    //锁过期时间(ms)
    private int expireTime;

    private RedisSupport redisSupport;

    private DistributeSync sync;

    public RedisReentrantLock(Pool<Jedis> pool, String lockName, int expireTime) {
        this(new RedisSupport(pool), lockName, expireTime);
    }

    public RedisReentrantLock(RedisSupport redisSupport, String lockName, int expireTime) {
        Assert.hasText(lockName, "lockName can not empty");
        Assert.isTrue(expireTime > 0, "localLock expire time must > business exec time");
        this.lockName = lockName;
        this.expireTime = expireTime;
        this.redisSupport = redisSupport;
        this.sync = new RedisSync();
    }

    @Override
    public boolean tryAcquire() throws DistributeLockException {
        return sync.tryAcquire();
    }

    @Override
    public boolean tryRelease() throws DistributeLockException {
        return sync.tryRelease();
    }

    private class RedisSync implements DistributeSync {

        private static final String UNLOCK_MESSAGE = "unlock";

        //当前锁的value
        private String lockValue;

        private int lockCount;

        private RedisSupport.JedisUnsuber unsuber;

        private ScheduledExecutorService service =
                Executors.newScheduledThreadPool(1);

        private Future<?> expireTaskFuture;

        private boolean isWaitUnLock = false;

        private RedisSupport.SubListener subListener = (channel, message) -> {
            try {
                log.info("redis unlock on message,channel:{},message:{}", channel, message);
                localLock.lock();
                //收到解锁通知，取消订阅
                if (lockName.equals(channel) && UNLOCK_MESSAGE.equals(message)) {
                    if (expireTaskFuture != null && !expireTaskFuture.isCancelled()
                            && !expireTaskFuture.isDone()) {
                        expireTaskFuture.cancel(false);
                        expireTaskFuture = null;
                    }
                    isWaitUnLock = false;
                    localCondition.signalAll();
                    log.info("redis unlock notify:channel={}", channel);
                    return false;
                }
                return true;
            } finally {
                localLock.unlock();
            }
        };

        /**
         * 加锁，失败注册监听,并获取过期时间
         * 使用lua脚本保证原子性
         */
        public boolean tryAcquire() throws DistributeLockException {
            if (lockCount > 0) {
                lockCount++;
                return true;
            }
            if (isWaitUnLock) {
                return false;
            }
            try {
                String value = UUID.randomUUID().toString();
                Long pttl;
                do {
                    boolean suc = redisSupport.setIfAbsent(lockName, value, expireTime);
                    if (suc) {
                        lockValue = value;
                        lockCount++;
                        log.info("redis distributeLock success:{}-->{}", lockName, lockValue);
                        return true;
                    }
                    //注册监听
                    unsuber = redisSupport.subAsync(subListener, lockName);
                    pttl = redisSupport.pttl(lockName);
                    //key不存在
                    if (RedisSupport.KEY_NOT_EXIST.equals(pttl)) {
                        unsuber.unsubscribe(lockName);
                        continue;
                    } else if (RedisSupport.KEY_PERSISTENT.equals(pttl)) {
                        throw new DistributeLockException("distributeLock key is persistent,key=" + lockName);
                    }
                    break;
                } while (true);

                //设置过期自动解锁
                expireTaskFuture = service.schedule(() -> {
                    try {
                        localLock.lock();
                        if (unsuber != null) {
                            unsuber.unsubscribe();
                        }
                        unsuber = null;
                        isWaitUnLock = false;
                        localCondition.signalAll();
                    } finally {
                        localLock.unlock();
                    }
                }, pttl, TimeUnit.MILLISECONDS);

                isWaitUnLock = true;
                return false;
            } catch (JedisException e) {
                throw new DistributeLockException(e);
            }
        }


        public boolean tryRelease() throws DistributeLockException {
            if (lockCount > 1) {
                lockCount--;
            } else if (lockCount == 0) {
                throw new IllegalMonitorStateException();
            } else {
                try {
                    String val = lockValue;
                    //删除key并发布解锁消息
                    Long delCount = redisSupport.eval(
                            "if redis.call('get',KEYS[1]) == ARGV[1] then " +
                                    "local c = redis.call('del',KEYS[1]) " +
                                    "redis.call('publish',KEYS[1],ARGV[2]) " +
                                    "return c " +
                                    "else " +
                                    "return 0 " +
                                    "end", Long.class, 1,
                            lockName, lockValue, UNLOCK_MESSAGE);

                    lockValue = null;
                    lockCount--;
                    //key过期或者已被其他进程加锁
                    if (delCount == 0) {
                        val = redisSupport.get(lockName);
                        throw new DistributeLockException(val == null ?
                                "distributeLock key '" + lockName + "' is already expire!" :
                                "other process has distributeLock! value is " + val);
                    } else {
                        log.info("redis unlock success,{}--->{}", lockName, val);
                    }
                } catch (JedisException e) {
                    throw new DistributeLockException(e);
                }
            }
            return true;
        }

    }


}
