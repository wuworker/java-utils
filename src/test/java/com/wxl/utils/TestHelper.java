package com.wxl.utils;

import java.io.*;

/**
 * Created by wuxingle on 2018/1/7 0007.
 * 测试帮助类
 */
public class TestHelper {


    public static InputStream getFileInputStream(Class<?> clazz, String fileName) {
        String name = clazz.getPackage().getName();
        String path = name.replaceAll("\\.", "/");
        return TestHelper.class.getClassLoader().getResourceAsStream(path + "/" + fileName);
    }

    public static OutputStream getFileOutputStream(Class<?> clazz, String fileName) {
        String name = clazz.getPackage().getName();
        String path = "src/test/resources/" + name.replaceAll("\\.", "/");
        File file;
        if (!(file = new File(path)).exists()) {
            file.mkdirs();
        }
        try {
            return new FileOutputStream(path + "/" + fileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


}
