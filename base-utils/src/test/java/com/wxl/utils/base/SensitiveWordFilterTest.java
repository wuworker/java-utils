package com.wxl.utils.base;

import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wuxingle on 2018/1/11.
 * 字符过滤
 */
public class SensitiveWordFilterTest {

    private static final String WORD = "澜湄六国地缘相近、人缘相亲、文缘相通。澜沧江-湄公河就像一条天然的“彩练”，" +
            "把我们紧紧联系在一起。2016年春天，我们在三亚举行首次领导法北人会议，全面启动澜湄合作进程。两闯故年来，我们心往" +
            "一处想、劲往一处使，推动澜湄合作皂串一步一个脚印从倡议变成现实，首次领导人会议确定的早期收获项目绝大部分已完" +
            "成或取得实质进园罗榨展，形成了“领导人引领、全方位覆盖、各部门参与”琉黍格局，创造了“天天有进展、月月有成果、年" +
            "年上台阶”的澜湄速度，培育了凳线腊台“俩敷响平等相待、真诚互助、亲如一家”的澜湄文鲸平化。";

    /**
     * 随机生成字库
     */
    @Test
    public void test1() throws IOException {
        Random random = new Random();
        List<String> list = RandomUtils.generateObject(random, 5000,
                (r) -> RandomUtils.generateChinese(r, 2, 5));

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(TestHelper.getFileOutputStream(
                SensitiveWordFilterTest.class, "sensitiveWord.txt")))) {

            for (String key : list) {
                out.write(key);
                out.write("\r\n");
            }
        }
    }

    /**
     * 测试正则
     */
    @Test
    public void testRegex() throws IOException {
        List<String> words = readWords();
        long t1 = System.currentTimeMillis();
        StringBuilder regex = new StringBuilder();
        for (String word : words) {
            regex.append(word).append("|");
        }
        long t2 = System.currentTimeMillis();

        List<String> find = new ArrayList<>();
        Pattern p = Pattern.compile(regex.substring(0, regex.length() - 1));
        Matcher matcher = p.matcher(WORD);
        while (matcher.find()) {
            find.add(matcher.group());
        }

        long t3 = System.currentTimeMillis();

        System.out.println(find);
        System.out.println("cost all:" + (t3 - t1) + ",match: " + (t3 - t2));
    }

    /**
     * 测试dfa
     */
    @Test
    public void testDFA()throws IOException {
        List<String> words = readWords();
        long t1 = System.currentTimeMillis();

        Map<Object,Object> map = SensitiveWordFilter.buildDFA(words);

        long t2 = System.currentTimeMillis();

        Set<String> find = SensitiveWordFilter.getSensitiveWord(map,WORD,SensitiveWordFilter.MIN_MATCH_TYPE);

        long t3 = System.currentTimeMillis();

        System.out.println(find);
        System.out.println("cost all:" + (t3 - t1) + ",match: " + (t3 - t2));
    }



    @Test
    public void test2() {
        Pattern pattern = Pattern.compile("ab|bc");
        Matcher matcher = pattern.matcher("12abcdef");
        while (matcher.find()) {
            String group = matcher.group();
            System.out.println(group);
        }
    }


    private static List<String> readWords() throws IOException {
        List<String> list = new ArrayList<>(1000);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                TestHelper.getFileInputStream(SensitiveWordFilterTest.class, "sensitiveWord.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        }
        return list;
    }


}





