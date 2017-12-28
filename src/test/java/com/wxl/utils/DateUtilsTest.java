package com.wxl.utils;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;

/**
 * Created by wuxingle on 2017/12/25.
 * 日期类测试
 */
public class DateUtilsTest {

    @Test
    public void testLocalDate(){
        LocalDate today = LocalDate.now();
        System.out.println(today);
        int y = today.getYear();
        int m = today.getMonthValue();
        int d = today.getDayOfMonth();
        System.out.println(y+"-"+m+"-"+d);

        LocalDate date2 = LocalDate.of(2017, 4, 20);
        System.out.println(date2);

        MonthDay birthday = MonthDay.of(4,17);
        MonthDay d1 = MonthDay.from(date2);
        MonthDay d2 = MonthDay.from(LocalDate.of(2000,4,17));
        System.out.println(birthday.equals(d1));
        System.out.println(birthday.equals(d2));


    }

    @Test
    public void testLocalTime(){
        LocalTime now = LocalTime.now();
        System.out.println(now);

        LocalTime t1 = now.plus(2, ChronoUnit.MINUTES);
        LocalTime t2 = now.plus(2, ChronoUnit.HOURS);
        System.out.println(t1);
        System.out.println(t2);


    }

























}


