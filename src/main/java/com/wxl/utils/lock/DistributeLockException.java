package com.wxl.utils.lock;

/**
 * Created by wuxingle on 2018/1/15.
 * 锁异常
 */
public class DistributeLockException extends RuntimeException{

    public DistributeLockException() {
        super();
    }

    public DistributeLockException(String message) {
        super(message);
    }

    public DistributeLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public DistributeLockException(Throwable cause) {
        super(cause);
    }
}
