package com.wxl.utils.net.soap;

/**
 * Created by wuxingle on 2018/1/12.
 * soap异常
 */
public class SoapException extends Exception{

    public SoapException() {
        super();
    }

    public SoapException(String message) {
        super(message);
    }

    public SoapException(String message, Throwable cause) {
        super(message, cause);
    }

    public SoapException(Throwable cause) {
        super(cause);
    }
}
