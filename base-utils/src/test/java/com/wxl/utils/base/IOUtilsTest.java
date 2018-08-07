package com.wxl.utils.base;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Create by wuxingle on 2018/6/23
 */
public class IOUtilsTest {

    @Test
    public void toByte() {

    }

    @Test
    public void toChar() {

    }

    @Test
    public void testString() throws IOException {
        String str = IOUtils.toString(new URL("https://www.baidu.com").openStream(), StandardCharsets.UTF_8);
        System.out.println(str);
    }

    @Test
    public void copy() {

    }
}