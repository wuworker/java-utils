package com.wxl.utils.base.lock;

import com.wxl.utils.base.annotation.ThreadSafe;

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
public abstract class AbstractDistributeLock implements DistributeLock, DistributeSync {

    protected Lock localLock = new ReentrantLock();

    protected Condition localCondition = localLock.newCondition();

    @Override
    public void lock() throws DistributeLockException {
        boolean suc = false;
        try {
            localLock.lock();
            while (!tryAcquire()) {
                localCondition.awaitUninterruptibly();
            }
            suc = true;
        } finally {
            if (!suc) {
                localLock.unlock();
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException, DistributeLockException {
        boolean suc = false;
        try {
            localLock.lock();
            while (!tryAcquire()) {
                localCondition.await();
            }
            suc = true;
        } finally {
            if (!suc) {
                localLock.unlock();
            }
        }
    }

    @Override
    public boolean tryLock() throws DistributeLockException {
        boolean local = false, remote = false;
        try {
            if (local = localLock.tryLock()) {
                return remote = tryAcquire();
            }
        } finally {
            //本地锁成功,远程锁失败
            if (local && !remote) {
                localLock.unlock();
            }
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException, DistributeLockException {
        boolean local = false, remote = false;
        long start = System.nanoTime();
        try {
            if (local = localLock.tryLock(time, unit)) {
                long use = System.nanoTime() - start;
                long need = unit.toNanos(time) - use;
                while (!tryAcquire()) {
                    long s = System.nanoTime();
                    if (!localCondition.await(need, TimeUnit.NANOSECONDS)) {
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
                localLock.unlock();
            }
        }
        return false;
    }

    @Override
    public void unlock() throws DistributeLockException {
        try {
            tryRelease();
        } finally {
            localLock.unlock();
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

}



