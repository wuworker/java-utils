package com.wxl.utils;

import com.wxl.utils.DataUtil;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by wuxingle on 2017/9/7 0007.
 *
 */
public class DataUtilTest {



    @Test
    public void toHex() throws Exception {
        byte[] bytes = {0x02,0x49,(byte)0xff};
        System.out.println(Arrays.toString(bytes));
        String hex = DataUtil.toHex(bytes);
        byte[] a = DataUtil.toByte("0249FF");
        System.out.println(hex);
        System.out.println(Arrays.toString(a));
    }

}