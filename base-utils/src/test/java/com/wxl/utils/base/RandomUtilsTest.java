package com.wxl.utils.base;

import org.junit.Test;

import java.util.Random;

/**
 * Created by wuxingle on 2017/12/9 0009.
 * 随机数测试
 */
public class RandomUtilsTest {

    @Test
    public void testInteger(){
        int i=1234;
        int a = 123456789;

        System.out.println(Integer.bitCount(i));
        System.out.println(Integer.bitCount(a));
    }

    @Test
    public void testGenerateNum(){
        Random random = new Random(10);
        String num1 = RandomUtils.generateNum(random,6);
        String num2 = RandomUtils.generateNum(random,3,8);
        String num3 = RandomUtils.generateNum(random,3,8);

        System.out.println(num1);
        System.out.println(num2);
        System.out.println(num3);
    }

    @Test
    public void testGenerateAbcNum(){
        Random random = new Random(10);
        String num1 = RandomUtils.generateAbcNum(random,6);
        String num2 = RandomUtils.generateAbcNum(random,3,8);
        String num3 = RandomUtils.generateAbcNum(random,3,8);

        System.out.println(num1);
        System.out.println(num2);
        System.out.println(num3);
    }


    @Test
    public void testGenerateSeeACSII(){
        Random random = new Random(10);
        String num1 = RandomUtils.generateSeeACSII(random,6);
        String num2 = RandomUtils.generateSeeACSII(random,3,8);
        String num3 = RandomUtils.generateSeeACSII(random,3,8);

        System.out.println(num1);
        System.out.println(num2);
        System.out.println(num3);
    }

    @Test
    public void testGenerateUUID(){
        String num1 = RandomUtils.generateUUID();
        String num2 = RandomUtils.generateUUID(true);
        String num3 = RandomUtils.generateUUID(false);

        System.out.println(num1);
        System.out.println(num2);
        System.out.println(num3);

    }

    @Test
    public void testGenerateDate(){
        Random random = new Random();
        String num1 = RandomUtils.generateDate(random,
                "2000-01-01 12:00:00","2017-09-09 12:00:00","yyyy-MM-dd HH:mm:ss");
        String num2 = RandomUtils.generateDate(random,
                "2000-01-01 12:00:00","2017-09-09 12:00:00","yyyy-MM-dd HH:mm:ss");
        String num3 = RandomUtils.generateDate(random,
                "2000-01-01 12:00:00","2017-09-09 12:00:00","yyyy-MM-dd HH:mm:ss");

        System.out.println(num1);
        System.out.println(num2);
        System.out.println(num3);
    }

    @Test
    public void testGenerateCandidate(){
        String[] can = "0,1,2,3,4,5,6,7,8,9,_,a,b,c".split(",");
        Random random = new Random(10);
        String num1 = RandomUtils.generateCandidate(random,6,can);
        String num2 = RandomUtils.generateCandidate(random,3,8,can);
        String num3 = RandomUtils.generateCandidate(random,3,8,can);

        System.out.println(num1);
        System.out.println(num2);
        System.out.println(num3);
    }


    @Test
    public void testGenerateMobilePhone(){
        Random random = new Random(10);
        String num1 = RandomUtils.generateMobilePhone(random);
        String num2 = RandomUtils.generateMobilePhone(random);
        String num3 = RandomUtils.generateMobilePhone(random);


        System.out.println(num1);
        System.out.println(num2);
        System.out.println(num3);
    }

    @Test
    public void testGenerateEmail(){
        Random random = new Random(10);
        String num1 = RandomUtils.generateEmail(random,8);
        String num2 = RandomUtils.generateEmail(random,3,8);
        String num3 = RandomUtils.generateEmail(random,3,8);


        System.out.println(num1);
        System.out.println(num2);
        System.out.println(num3);
    }


    @Test
    public void testGenerateChinese(){
        Random random = new Random(198);
        String num1 = RandomUtils.generateChinese(random,8);
        String num2 = RandomUtils.generateChinese(random,3,8);
        String num3 = RandomUtils.generateChinese(random,3,8);


        System.out.println(num1);
        System.out.println(num2);
        System.out.println(num3);
    }




}