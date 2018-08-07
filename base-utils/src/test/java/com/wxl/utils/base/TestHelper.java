package com.wxl.utils.base;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by wuxingle on 2018/1/7 0007.
 * 测试帮助类
 */
public class TestHelper {

    public static final String TEST_FILE_PATH_PRE = "src/test/resources/";


    public static InputStream getFileInputStream(Class<?> clazz, String fileName) {
        String path = convertPackageToPath(clazz);
        return TestHelper.class.getClassLoader().getResourceAsStream(path + fileName);
    }

    public static FileOutputStream getFileOutputStream(Class<?> clazz, String fileName) {
        try {
            return new FileOutputStream(getFile(clazz,fileName));
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static File getFile(Class<?> clazz, String fileName) {
        Path path = Paths.get(getRelativePath(clazz, fileName));
        Path parent = path.getParent();
        try {
            if(!Files.exists(parent)){
                Files.createDirectories(parent);
            }
            return path.toFile();
        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    public static String getRelativePath(Class<?> clazz, String filename) {
        return TEST_FILE_PATH_PRE + convertPackageToPath(clazz) + filename;
    }


    public static String convertPackageToPath(Class<?> clazz) {
        String name = clazz.getPackage().getName();
        return name.replaceAll("\\.", "/") + "/";
    }
}

