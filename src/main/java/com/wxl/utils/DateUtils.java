package com.wxl.utils;

import org.springframework.util.Assert;

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
        Assert.notNull(date,"date can not null");
        Assert.notNull(dateFormat,"dateFormat can not null");
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
        Assert.hasText(date,"date can not empty");
        Assert.notNull(dateFormat,"dateFormat can not null");
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }











}
