package com.wxl.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wuxingle on 2017/12/9 0009.
 * 日期相关工具类
 */
public class DateUtils {

    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";


    /**
     * 日期格式化
     */
    public static String format(Date date){
        return format(date,DEFAULT_FORMAT);
    }

    public static String format(Date date,String format){
        return format(date,new SimpleDateFormat(format));
    }

    public static String format(Date date,DateFormat dateFormat){
        return dateFormat.format(date);
    }

    /**
     * 日期解析
     */
    public static Date parse(String date){
        return parse(date,DEFAULT_FORMAT);
    }

    public static Date parse(String date,String dateFormat){
        return parse(date,new SimpleDateFormat(dateFormat));
    }

    public static Date parse(String date,DateFormat dateFormat){
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 输入日期是否是将来的时间
     * @param date 输入日期
     */
    public static boolean isFuture(Date date){
        return date.getTime() - System.currentTimeMillis() > 0;
    }

    public static boolean isFuture(String date,String format){
        return isFuture(parse(date,format));
    }


    /**
     * 当前时间是否在输入时间内
     */
    public static boolean nowInTime(Date start,Date end){
        long now = System.currentTimeMillis();
        return now - start.getTime() > 0
                && end.getTime() - now > 0;
    }

    public static boolean nowInTime(String start,String end,String format){
        SimpleDateFormat df = new SimpleDateFormat(format);
        return nowInTime(parse(start,df),parse(end,df));
    }






}
