package com.wxl.utils.base;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wuxingle on 2017/12/9 0009.
 * 随机数工具类
 */
public class RandomUtils {


    public interface RandomGenerator<T> {
        T generate(Random random);
    }


    /**
     * 手机号码前缀
     * 移动：134/135/136/137/138/139/150/151/152/157/158/159/182/183/184/187/188/147/178
     * 联通：130/131/132/155/156/185/186/145/176
     * 电信：133/153/180/181/189/177
     * 虚拟运营商:170
     */
    private static final String[] MOBILE_PHONE_PREFIX = (
            "134/135/136/137/138/139/150/151/152/157/158/159/182/183/184/187/188/147/178/"
                    + "130/131/132/155/156/185/186/145/176/"
                    + "133/153/180/181/189/177"
    ).split("/");

    //邮箱后缀
    private static final String[] EMAIL_SUFFIX = (
            "@gmail.com,@yahoo.com,@msn.com,@hotmail.com,"
                    + "@aol.com,@ask.com,@live.com,@qq.com,@0355.net,@163.com,@163.net,@263.net,@3721.net,"
                    + "@yeah.net,@googlemail.com,@126.com,@sina.com,@sohu.com,@yahoo.com.cn"
    ).split(",");


    /**
     * 产生一定范围的int
     */
    public static int generateInt(int min, int max) {
        return generateInt(new Random(), min, max);
    }

    public static int generateInt(Random random, int min, int max) {
        Assert.notNull(random, "random can not null");
        Assert.isTrue(max > min && min >= 0, "max must > min and >= 0");
        return random.nextInt(max - min) + min;
    }


    /**
     * 产生随机数字字符串
     */
    public static String generateNum(int length) {
        return generateNum(new Random(), length);
    }

    public static String generateNum(int min, int max) {
        return generateNum(new Random(), min, max);
    }

    public static String generateNum(Random random, int length) {
        Assert.notNull(random, "random can not null");
        Assert.isTrue(length > 0, "length must > 0");
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String generateNum(Random random, int min, int max) {
        Assert.isTrue(max > min && min > 0, "max must > min and > 0");
        int len = random.nextInt(max - min) + min;
        return generateNum(random, len);
    }

    /**
     * 产生随机数字字母组合
     */
    public static String generateAbcNum(int length) {
        return generateAbcNum(new Random(), length);
    }

    public static String generateAbcNum(int min, int max) {
        return generateAbcNum(new Random(), min, max);
    }

    public static String generateAbcNum(Random random, int length) {
        Assert.notNull(random, "random can not null");
        Assert.isTrue(length > 0, "length must > 0");
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int n = random.nextInt(62);
            if (n < 10) {
                sb.append(n);
            } else if (n < 36) {
                sb.append((char) ('a' - 10 + n));
            } else {
                sb.append((char) ('A' - 36 + n));
            }
        }
        return sb.toString();
    }

    public static String generateAbcNum(Random random, int min, int max) {
        Assert.isTrue(max > min && min > 0, "max must > min and > 0");
        int len = random.nextInt(max - min) + min;
        return generateAbcNum(random, len);
    }

    /**
     * 产生随机可见ASCII码组合
     */
    public static String generateSeeACSII(int length) {
        return generateSeeACSII(new Random(), length);
    }

    public static String generateSeeACSII(int min, int max) {
        return generateSeeACSII(new Random(), min, max);
    }

    public static String generateSeeACSII(Random random, int length) {
        Assert.notNull(random, "random can not null");
        Assert.isTrue(length > 0, "length must > 0");
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) (random.nextInt(93) + 33));
        }
        return sb.toString();
    }

    public static String generateSeeACSII(Random random, int min, int max) {
        Assert.isTrue(max > min && min > 0, "max must > min and > 0");
        int len = random.nextInt(max - min) + min;
        return generateSeeACSII(random, len);
    }

    /**
     * 产生随机uuid
     */
    public static String generateUUID() {
        return generateUUID(false);
    }

    public static String generateUUID(boolean format) {
        return format ? UUID.randomUUID().toString().replaceAll("-", "")
                : UUID.randomUUID().toString();
    }


    /**
     * 生成随机日期
     *
     * @param startDate 开始日期字符串
     * @param endDate   结束日期字符串
     * @param format    格式化字符
     */
    public static String generateDate(String startDate, String endDate, String format) {
        return generateDate(new Random(), startDate, endDate, format);
    }

    public static String generateDate(Random random, String startDate, String endDate, String format) {
        Assert.notNull(random, "random can not null");
        Assert.hasText(startDate, "startDate can not empty");
        Assert.hasText(endDate, "endDate can not empty");
        Assert.hasText(format, "format can not empty");
        DateFormat df = new SimpleDateFormat(format);
        Date start = DateUtils.parse(startDate, df);
        Date end = DateUtils.parse(endDate, df);
        return DateUtils.format(generateDate(random, start, end), df);
    }

    public static Date generateDate(Date start, Date end) {
        return generateDate(new Random(), start, end);
    }

    public static Date generateDate(Random random, Date start, Date end) {
        Assert.notNull(random, "random can not null");
        Assert.notNull(start, "start can not null");
        Assert.notNull(end, "end can not null");
        long t = (long) ((end.getTime() - start.getTime()) * random.nextDouble()) + start.getTime();
        return new Date(t);
    }


    /**
     * 从候选数组中随机生成字符串
     */
    public static String generateCandidate(int length, String[] candidates) {
        return generateCandidate(new Random(), length, candidates);
    }

    public static String generateCandidate(int min, int max, String[] candidates) {
        return generateCandidate(new Random(), min, max, candidates);
    }

    public static String generateCandidate(Random random, int length, String[] candidates) {
        Assert.notNull(random, "random can not null");
        Assert.isTrue(length > 0, "length must > 0");
        Assert.isTrue(!ObjectUtils.isEmpty(candidates), "candidates can not empty");
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(candidates[random.nextInt(candidates.length)]);
        }
        return sb.toString();
    }

    public static String generateCandidate(Random random, int min, int max, String[] candidates) {
        Assert.isTrue(max > min && min > 0, "max must > min and > 0");
        int len = random.nextInt(max - min) + min;
        return generateCandidate(random, len, candidates);
    }


    /**
     * 随机生成手机号
     */
    public static String generateMobilePhone() {
        return generateMobilePhone(new Random());
    }

    public static String generateMobilePhone(Random random) {
        Assert.notNull(random, "random can not null");
        String prefix = MOBILE_PHONE_PREFIX[random.nextInt(MOBILE_PHONE_PREFIX.length)];
        int end = 11 - prefix.length();
        return prefix + generateNum(random, end);
    }

    /**
     * 随机生成邮箱
     */
    public static String generateEmail(int length) {
        return generateEmail(new Random(), length);
    }

    public static String generateEmail(int min, int max) {
        return generateEmail(new Random(), min, max);
    }

    public static String generateEmail(Random random, int length) {
        Assert.notNull(random, "random can not null");
        Assert.isTrue(length > 0, "length must > 0");
        String suffix = EMAIL_SUFFIX[random.nextInt(EMAIL_SUFFIX.length)];
        return generateAbcNum(random, length) + suffix;
    }

    public static String generateEmail(Random random, int min, int max) {
        Assert.isTrue(max > min && min > 0, "max must > min and > 0");
        int len = random.nextInt(max - min) + min;
        return generateEmail(random, len);
    }

    /**
     * 随机生成常用汉字
     */
    public static String generateChinese(int length) {
        return generateChinese(new Random(), length);
    }

    public static String generateChinese(int min, int max) {
        return generateChinese(new Random(), min, max);
    }

    public static String generateChinese(Random random, int length) {
        Assert.notNull(random, "random can not null");
        Assert.isTrue(length > 0, "length must > 0");
        StringBuilder sb = new StringBuilder(length);
        try {
            byte[] b = new byte[2];
            for (int i = 0; i < length; i++) {
                b[0] = (byte) (176 + random.nextInt(39));
                b[1] = (byte) (161 + random.nextInt(93));
                sb.append(new String(b, "gbk"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        return sb.toString();
    }

    public static String generateChinese(Random random, int min, int max) {
        Assert.isTrue(max > min && min > 0, "max must > min and > 0");
        int len = random.nextInt(max - min) + min;
        return generateChinese(random, len);
    }


    /**
     * 随机生成一系列对象
     */
    public static <T> List<T> generateObject(int length, RandomGenerator<T> randomGenerator) {
        return generateObject(new Random(), length, randomGenerator);
    }

    public static <T> List<T> generateObject(int min, int max, RandomGenerator<T> randomGenerator) {
        return generateObject(new Random(), min, max, randomGenerator);
    }

    public static <T> List<T> generateObject(Random random, int length, RandomGenerator<T> randomGenerator) {
        Assert.notNull(random, "random can not null");
        Assert.isTrue(length > 0, "length must > 0");
        Assert.notNull(randomGenerator, "randomGenerator can not null");
        List<T> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            list.add(randomGenerator.generate(random));
        }
        return list;
    }

    public static <T> List<T> generateObject(Random random, int min, int max, RandomGenerator<T> randomGenerator) {
        Assert.isTrue(max > min && min > 0, "max must > min and > 0");
        int len = random.nextInt(max - min) + min;
        return generateObject(random, len, randomGenerator);
    }


}
