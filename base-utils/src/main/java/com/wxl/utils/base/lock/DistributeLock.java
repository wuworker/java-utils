package com.wxl.utils.base.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Created by wuxingle on 2018/1/15.
 * 分布式锁
 */
public interface DistributeLock extends Lock {

    void lock() throws DistributeLockException;

    void lockInterruptibly() throws InterruptedException, DistributeLockException;

    boolean tryLock() throws DistributeLockException;

    boolean tryLock(long time, TimeUnit unit) throws InterruptedException, DistributeLockException;

    void unlock() throws DistributeLockException;

}













