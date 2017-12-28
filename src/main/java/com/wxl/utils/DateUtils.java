package com.wxl.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wuxingle on 2017/12/9 0009.
 * 日期相关工具类
 */
public class DateUtils {

    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";


    /**
     * 日期格式化
     */
    public static String format(Date date, String format) {
        return format(date, new SimpleDateFormat(format));
    }

    public static String format(Date date, DateFormat dateFormat) {
        return dateFormat.format(date);
    }

    /**
     * 日期解析
     */
    public static Date parse(String date, String dateFormat) {
        return parse(date, new SimpleDateFormat(dateFormat));
    }

    public static Date parse(String date, DateFormat dateFormat) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 输入日期是否是将来的时间
     *
     * @param date 输入日期
     */
    public static boolean isFuture(Date date) {
        return date.getTime() - System.currentTimeMillis() > 0;
    }

    public static boolean isFuture(String date, String format) {
        return isFuture(parse(date, format));
    }


    /**
     * 当前时间是否在输入时间内
     */
    public static boolean nowInTime(Date start, Date end) {
        Date now = new Date();
        return start.before(now)
                && end.after(now);
    }

    public static boolean nowInTime(String start, String end, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return nowInTime(parse(start, df), parse(end, df));
    }


    /**
     * 获取几天前的日期
     *
     * @param dayAgo 几天前
     */
    public static Date getDayAgo(int dayAgo) {
        return new Date(System.currentTimeMillis() - (1000L * 24 * 3600 * dayAgo));
    }

    public static Date getDayAgo(Date date, int dayAgo) {
        return new Date(date.getTime() - (1000L * 24 * 3600 * dayAgo));
    }

    public static String getDayAgo(String date, int dayAgo, String format) {
        return format(getDayAgo(parse(date,format), dayAgo), format);
    }


    /**
     * 获取2个日期的天数差
     * day2 - day1
     */
    public static int diffDay(Date day1, Date day2) {
        ZoneId zoneId = ZoneId.systemDefault();
        Period period = Period.between(day1.toInstant().atZone(zoneId).toLocalDate()
                , day2.toInstant().atZone(zoneId).toLocalDate());
        return period.getDays();
    }

    public static int diffDay(String day1, String day2, String format) {
        DateFormat df = new SimpleDateFormat(format);
        Date d1 = parse(day1, df);
        Date d2 = parse(day2, df);
        return diffDay(d1, d2);
    }


    /**
     * 获取2个日期之间的所有Date
     * 间隔为天
     * @param start 包含起始时间
     * @param end   不包含结束时间
     */
    public static List<Date> getBetweenDays(Date start, Date end) {
        int diff = diffDay(start, end);
        if (diff < 0) {
            return new ArrayList<>();
        }
        List<Date> list = new ArrayList<>(diff);
        for (int i = 0; i < diff; i++) {
            list.add(getDayAgo(start, -i));
        }
        return list;
    }

    public static List<String> getBetweenDays(String start, String end, String format) {
        DateFormat df = new SimpleDateFormat(format);
        Date s = parse(start, df);
        Date e = parse(end, df);
        int diff = diffDay(s, e);
        if (diff < 0) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>(diff);
        for (int i = 0; i < diff; i++) {
            list.add(format(getDayAgo(s, -i), df));
        }
        return list;
    }


}




















