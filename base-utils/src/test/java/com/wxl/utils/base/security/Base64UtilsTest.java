package com.wxl.utils.base.security;

import org.junit.Test;

/**
 * Created by wuxingle on 2017/9/7 0007.
 *
 */
public class Base64UtilsTest {

    @Test
    public void test() throws Exception {
        String pass = "哈很多事发";
        String en = Base64Utils.encodeToString(pass.getBytes());
        System.out.println(en);
        byte[] de = Base64Utils.decodeFromString(en);
        System.out.println(new String(de));
    }

}