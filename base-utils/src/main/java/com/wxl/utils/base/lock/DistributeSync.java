package com.wxl.utils.base.lock;

import com.wxl.utils.base.annotation.UnThreadSafe;

/**
 * Create by wuxingle on 2018/4/7
 * 分布式锁的加锁，解锁接口
 * 接口只保证多进程安全，不保证线程安全
 * 如果需要线程安全要由使用者保证
 */
@UnThreadSafe
public interface DistributeSync {

    /**
     * 加锁
     */
    boolean tryAcquire() throws DistributeLockException;

    /**
     * 解锁
     */
    boolean tryRelease() throws DistributeLockException;

}
