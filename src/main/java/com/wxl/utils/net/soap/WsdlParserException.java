package com.wxl.utils.net.soap;

/**
 * Created by wuxingle on 2018/1/11.
 * wsdl解析异常
 */
public class WsdlParserException extends Exception{

    public WsdlParserException() {
        super();
    }

    public WsdlParserException(String message) {
        super(message);
    }

    public WsdlParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public WsdlParserException(Throwable cause) {
        super(cause);
    }
}
