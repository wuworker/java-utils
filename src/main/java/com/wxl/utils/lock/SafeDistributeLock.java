package com.wxl.utils.lock;

import com.wxl.utils.annotation.ThreadSafe;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Create by wuxingle on 2018/4/7
 * 线程安全的分布式锁
 * 需要同DistributeSync配合
 * 这个类只保证客户端安全和await
 * 唤醒需要自行实现
 */
@ThreadSafe
public class SafeDistributeLock implements DistributeLock {

    private final DistributeSync sync;

    private Lock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    public SafeDistributeLock(DistributeSync sync) {
        Assert.notNull(sync, "distribute sync can not null");
        this.sync = sync;
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

    /**
     * 获取本地锁
     */
    public Lock getLocalLock(){
        return lock;
    }

    /**
     * 获取本地condition
     */
    public Condition getLocalCondition(){
        return condition;
    }

}



