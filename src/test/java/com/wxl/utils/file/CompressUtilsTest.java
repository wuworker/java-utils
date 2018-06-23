package com.wxl.utils.file;

import com.wxl.utils.DataUtils;
import com.wxl.utils.RandomUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.wxl.utils.TestHelper.*;
import static org.junit.Assert.*;

/**
 * Create by wuxingle on 2018/6/23
 */
public class CompressUtilsTest {


    @Test
    public void testZip() throws IOException {
        File zipFile = file("testzip1.zip");
        CompressUtils.zip(zipFile, file("test1.txt"), file("test2.txt"), file("d1"));
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