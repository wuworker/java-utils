package com.wxl.utils;

import org.springframework.util.StringUtils;

/**
 * Created by wuxingle on 2017/11/24.
 * 别名工具类
 */
public class AliasUtil {

    /**
     * 蛇形命名转驼峰
     */
    public static String snakeToCamel(String name) {
        if (!StringUtils.hasText(name)) {
            return name;
        }
        int index = 0;
        while ((index = name.indexOf("_")) != -1) {
            //最后一位
            if (index == name.length() - 1) {
                return name.substring(0, name.length() - 1);
            }
            char c = name.charAt(index + 1);
            name = name.substring(0, index)
                    + Character.toUpperCase(c)
                    + name.substring(index + 2, name.length());
        }
        return name;
    }

    /**
     * 驼峰命名转蛇形
     */
    public static String camelToSnake(String name) {
        if (!StringUtils.hasText(name)) {
            return name;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                name = name.substring(0, i)
                        + "_"
                        + Character.toLowerCase(c)
                        + name.substring(i + 1, name.length());
                i = i + 1;
            }
        }
        return name;
    }


}




