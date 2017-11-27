package com.wxl.utils.net.http;

/**
 * Created by wuxingle on 2017/10/20.
 * http的请求方法
 */
public class HttpMethod {

    public static final String GET = "GET";
    public static final String HEAD = "HEAD";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String OPTIONS = "OPTIONS";
    public static final String TRACE = "TRACE";
    public static final String PATCH = "PATCH";
    public static final String CONNECT = "CONNECT";

    public static final String[] HTTP_METHODS = {
            GET,HEAD,POST,PUT,DELETE,OPTIONS,TRACE,PATCH,CONNECT
    };

    /**
     * 是否是正确的http方法
     */
    public static boolean isCorrectMethod(String method){
        if(method == null){
            return false;
        }
        String bigMethod = method.toUpperCase();
        for(String realMethod:HTTP_METHODS){
            if(realMethod.equals(bigMethod)){
                return true;
            }
        }
        return false;
    }

    /**
     * 是否支持发送请求体
     */
    public static boolean supportedSendBody(String method){
        if(!isCorrectMethod(method)){
            return false;
        }
        String upMethod = method.toUpperCase();
        return POST.equals(upMethod)
                || PUT.equals(upMethod)
                || PATCH.equals(upMethod);
    }

}












