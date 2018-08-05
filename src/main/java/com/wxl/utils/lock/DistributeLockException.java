package com.wxl.utils.lock;

/**
 * Created by wuxingle on 2018/1/15.
 * 锁异常
 */
public class DistributeLockException extends RuntimeException{

    private static final long serialVersionUID = 4025395848369526629L;

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
