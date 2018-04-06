package com.wxl.utils.lock.redis;

import com.wxl.utils.lock.DistributeLock;
import com.wxl.utils.lock.DistributeLockException;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wuxingle on 2018/03/12
 * redis的分布式锁
 */
@Slf4j
public class RedisReentrantLock extends RedisSupport implements DistributeLock {

    private String lockName;


    //锁过期时间
    private int expireTime;

    private Lock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    private Sync sync;

    public RedisReentrantLock(Pool<Jedis> pool, String lockName, int expireTime) {
        super(pool);
        this.lockName = lockName;
        this.expireTime = expireTime;
        sync = new Sync();
    }


    class Sync {

        //当前锁的value
        private String lockValue;

        private int lockCount;

        private JedisUnsuber unsuber;

        private ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

        private Future<?> expireTaskFuture;

        private SubListener subListener = new SubListener() {
            @Override
            public boolean onMessage(String channel, String message) {
                try {
                    lock.lock();
                    //收到解锁通知，取消订阅
                    if (lockName.equals(channel) && "unlock".equals(message)) {
                        if (expireTaskFuture != null
                                && !expireTaskFuture.isCancelled()
                                && !expireTaskFuture.isDone()) {
                            expireTaskFuture.cancel(false);
                            expireTaskFuture = null;
                        }
                        condition.signalAll();
                        return false;
                    }
                    return true;
                } finally {
                    lock.unlock();
                }
            }
        };


        public boolean tryAcquire() throws DistributeLockException {
            if (lockCount > 0) {
                lockCount++;
                return true;
            }
            try {
                String value = UUID.randomUUID().toString();
                boolean suc = setIfAbsent(lockName, value, expireTime);
                if (suc) {
                    lockValue = value;
                    lockCount++;
                    log.info("redis lock success:{}-->{}", lockName, lockValue);
                    return true;
                }

                //注册监听
                unsuber = subAsync(subListener, lockName);
                expireTaskFuture = service.schedule(() -> {
                    if (unsuber != null) {
                        unsuber.unsubscribe();
                    }
                }, 100, TimeUnit.SECONDS);

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
                    boolean suc = del(lockName, lockValue);
                    lockValue = null;
                    lockCount--;
                    //key过期或者已被其他进程加锁
                    if (!suc) {
                        val = get(lockName);
                        throw new DistributeLockException(val == null ?
                                "lock key '" + lockName + "' is already expire!" :
                                "other process has lock! value is " + val);
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


    @Override
    public void lock() throws DistributeLockException {
        boolean suc = false;
        try {
            lock.lock();
            while (!sync.tryAcquire()) {
                condition.awaitUninterruptibly();
            }
            suc = true;
        } finally {
            if (!suc) {
                lock.unlock();
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException, DistributeLockException {
        boolean suc = false;
        try {
            lock.lock();
            while (!sync.tryAcquire()) {
                condition.await();
            }
            suc = true;
        } finally {
            if (!suc) {
                lock.unlock();
            }
        }
    }

    @Override
    public boolean tryLock() throws DistributeLockException {
        boolean local = false, remote = false;
        try {
            if (local = lock.tryLock()) {
                return remote = sync.tryAcquire();
            }
        } finally {
            //本地锁成功,远程锁失败
            if (local && !remote) {
                lock.unlock();
            }
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException, DistributeLockException {
        boolean local = false, remote = false;
        long start = System.nanoTime();
        try {
            if (local = lock.tryLock(time, unit)) {
                long use = System.nanoTime() - start;
                long need = unit.toNanos(time) - use;
                while (!sync.tryAcquire()) {
                    long s = System.nanoTime();
                    if (!condition.await(need, TimeUnit.NANOSECONDS)) {
                        return false;
                    }
                    need = need - (System.nanoTime() - s);
                    if (need <= 0) {
                        return false;
                    }
                }
                return remote = true;
            }
        } finally {
            if (local && !remote) {
                lock.unlock();
            }
        }
        return false;
    }

    @Override
    public void unlock() throws DistributeLockException {
        try {
            sync.tryRelease();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

}
