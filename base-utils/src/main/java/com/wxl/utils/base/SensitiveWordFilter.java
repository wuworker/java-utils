package com.wxl.utils.base;

import java.util.*;

/**
 * Created by wuxingle on 2018/1/11.
 * 敏感词过滤
 * DFA算法
 * http://cmsblogs.com/?p=1031
 */
public class SensitiveWordFilter {

    //最小匹配
    public static final int MIN_MATCH_TYPE = 1;

    //最大匹配
    public static final int MAX_MATCH_TYPE = 2;

    /**
     * 构建dfa结构,用以实现敏感词过滤
     * 如果数据量大的话可以单独缓存
     *
     * @param words 词库
     */
    @SuppressWarnings("unchecked")
    public static Map<Object, Object> buildDFA(Collection<String> words) {
        Map dfa = new HashMap(words.size());
        Map nowMap;
        Map wordMap;
        for (String word : words) {
            nowMap = dfa;
            for (int i = 0, len = word.length(); i < len; i++) {
                char key = word.charAt(i);
                Object v = nowMap.get(key);
                if (v != null) {
                    nowMap = (Map) v;
                } else {
                    wordMap = new HashMap();
                    wordMap.put("isEnd", false);
                    nowMap.put(key, wordMap);
                    nowMap = wordMap;
                }
                if (i == len - 1) {
                    nowMap.put("isEnd",true);
                }
            }
        }
        return dfa;
    }


    /**
     * 是否包含敏感词
     *
     * @param words 词库
     */
    public static boolean containsSensitiveWord(Collection<String> words, String text) {
        return containsSensitiveWord(buildDFA(words), text);
    }

    public static boolean containsSensitiveWord(Map<Object, Object> dfa, String text) {
        for (int i = 0, len = text.length(); i < len; i++) {
            int l = checkSensitiveWord(dfa, text, i, MIN_MATCH_TYPE);
            if (l > 0) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取敏感词
     *
     * @param words 词库
     */
    public static Set<String> getSensitiveWord(Collection<String> words, String text, int matchType) {
        return getSensitiveWord(buildDFA(words), text, matchType);
    }

    public static Set<String> getSensitiveWord(Map<Object, Object> dfa, String text, int matchType) {
        Set<String> findWords = new LinkedHashSet<>();
        for (int i = 0, len = text.length(); i < len; i++) {
            int l = checkSensitiveWord(dfa, text, i, matchType);
            if (l > 0) {
                findWords.add(text.substring(i, i + l));
                i = i + l - 1;
            }
        }
        return findWords;
    }

    /**
     * 替换敏感词
     *
     * @param words 词库
     */
    public static String replaceSensitiveWord(Collection<String> words, String text, String replace, int matchType) {
        return replaceSensitiveWord(buildDFA(words), text, replace, matchType);
    }

    public static String replaceSensitiveWord(Map<Object, Object> dfa, String text, String replace, int matchType) {
        Set<String> sensitiveWords = getSensitiveWord(dfa, text, matchType);
        for (String sensitiveWord : sensitiveWords) {
            String replaceString = getReplaceString(replace, sensitiveWord.length());
            text = text.replace(sensitiveWord, replaceString);
        }
        return text;
    }


    private static String getReplaceString(String replace, int len) {
        int rlen = replace.length();
        StringBuilder sb = new StringBuilder(rlen * len);
        for (int i = 0; i < len; i++) {
            sb.append(replace);
        }
        return sb.toString();
    }


    @SuppressWarnings({"rawtypes"})
    private static int checkSensitiveWord(Map<Object, Object> dfa, String txt, int beginIndex, int matchType) {
        int matchFlag = 0;
        char word;
        Map nowMap = dfa;
        boolean match = false;
        for (int i = beginIndex; i < txt.length(); i++) {
            word = txt.charAt(i);
            nowMap = (Map) nowMap.get(word);
            if (nowMap == null) {
                return match ? matchFlag : 0;
            }
            matchFlag++;
            if (Boolean.TRUE.equals(nowMap.get("isEnd"))) {
                match = true;
                if (MIN_MATCH_TYPE == matchType || i == txt.length() - 1) {
                    return matchFlag;
                }
            }
        }
        return 0;
    }


}



