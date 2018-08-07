package com.wxl.utils.base;

import org.junit.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * Created by wuxingle on 2017/12/25.
 * 日期类测试
 */
public class DateUtilsTest {

    @Test
    public void testIsFuture(){
        System.out.println(DateUtils.isFuture(
                "2017-12-28 22:00:12",
                "yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    public void testNowInTime(){
        String s1 = "2017-12-28 22:00:12";
        String s2 = "2017-12-29 22:00:12";

        System.out.println(DateUtils.nowInTime(
                s1,s2,"yyyy-MM-dd HH:mm:ss"));
        System.out.println(DateUtils.nowInTime(
                s1,s2,"yyyy-MM-dd"));
    }

    @Test
    public void testDayAgo(){
        String d1 = DateUtils.getDayAgo("2017-12-28 22:00:12",
                3,"yyyy-MM-dd");
        System.out.println(d1);
    }

    @Test
    public void testDiffDay(){
        String d1 = "2017-12-28 22:00:12";
        String d2 = "2017-12-31 01:00:12";

        int diff = DateUtils.betweenDay(d1,d2,"yyyy-MM-dd HH:mm:ss");
        System.out.println(diff);

        Date dd1 = DateUtils.parse(d1,"yyyy-MM-dd HH:mm:ss");
        Date dd2 = DateUtils.parse(d2,"yyyy-MM-dd HH:mm:ss");

        System.out.println((dd2.getTime()-dd1.getTime())/(24*1000*3600));
    }


    @Test
    public void testGetBetweetDays(){
        String d1 = "2017-12-28 22:00:12";
        String d2 = "2017-12-31 01:00:12";

        List<String> list = DateUtils.getBetweenDays(d1,d2,"yyyy-MM-dd HH:mm:ss");
        System.out.println(list);

        List<String> list2 = DateUtils.getBetweenDays(d1,d2,"yyyy-MM-dd");
        System.out.println(list2);
    }


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

        LocalDate d3 = today.plus(1,ChronoUnit.DAYS);
        System.out.println(d3);

        LocalDate d4 = today.plus(-1,ChronoUnit.DAYS);
        System.out.println(d4);

        LocalDate d5 = today.minusDays(1);
        System.out.println(d5);

        System.out.println(d5.isBefore(today));

        Period period = Period.between(today,d5);
        System.out.println(period.getDays());

        System.out.println(Period.between(d5,today).getDays());
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


    @Test
    public void testClock() {
        Clock clock = Clock.systemUTC();
        System.out.println(clock);
        System.out.println(clock.millis());

        Instant instant = Instant.now(clock);
        System.out.println(instant.toEpochMilli());
        System.out.println(instant.getEpochSecond());

        boolean supported = Instant.now().isSupported(ChronoUnit.DAYS);
        System.out.println(supported);
    }

    @Test
    public void testFormatter(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.now();
        System.out.println(dateTime.format(formatter));
        System.out.println(LocalDateTime.parse("2017年08月10日 12:23:45",formatter));
    }



}


