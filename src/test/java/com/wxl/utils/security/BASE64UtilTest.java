package com.wxl.utils.security;

import org.junit.Test;

/**
 * Created by wuxingle on 2017/9/7 0007.
 *
 */
public class BASE64UtilTest {

    @Test
    public void test() throws Exception {
        String pass = "哈很多事发";
        String en = BASE64Util.encodeToString(pass.getBytes());
        System.out.println(en);
        byte[] de = BASE64Util.decodeFromString(en);
        System.out.println(new String(de));
    }

}