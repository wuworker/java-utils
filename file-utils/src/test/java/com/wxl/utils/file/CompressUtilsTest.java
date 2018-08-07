package com.wxl.utils.file;

import com.wxl.utils.base.IOUtils;
import com.wxl.utils.base.RandomUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.wxl.utils.file.TestHelper.getFile;
import static com.wxl.utils.file.TestHelper.getRelativePath;
import static org.junit.Assert.assertArrayEquals;

/**
 * Create by wuxingle on 2018/6/23
 */
public class CompressUtilsTest {

    @Test
    public void test() throws IOException{
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file("testzip1.zip")));
        out.putNextEntry(new ZipEntry("test1.txt"));
        IOUtils.copy(new FileInputStream(file("test1.txt")),out,true,true);

    }

    @Test
    public void testZip() throws IOException {
        File zipFile = file("testzip1.zip");
        CompressUtils.zip(zipFile, file("test1.txt"),file("test2.txt"),file("d1"));
        System.out.println("zip success");

        CompressUtils.unzip(zipFile, getRelativePath(CompressUtilsTest.class, "d2"), true);
        System.out.println("unzip success");
    }

    @Test
    public void testGzip() throws IOException {
        byte[] bytes = RandomUtils.generateAbcNum(10000).getBytes();
        System.out.println(bytes.length);

        byte[] gzip = CompressUtils.gzip(bytes);
        System.out.println(gzip.length);

        byte[] ungzip = CompressUtils.ungzip(gzip);
        System.out.println(ungzip.length);

        assertArrayEquals(bytes, ungzip);
    }


    private static File file(String name) {
        return getFile(CompressUtilsTest.class, name);
    }


}