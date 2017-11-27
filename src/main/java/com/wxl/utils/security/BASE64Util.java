package com.wxl.utils.security;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;

/**
 * Created by wuxingle on 2017/9/7 0007.
 * base64工具类
 */
public class BASE64Util {


    private static final byte[] pem_array = {
            // 0 1 2 3 4 5 6 7
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', // 0
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', // 1
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', // 2
            'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', // 3
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', // 4
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', // 5
            'w', 'x', 'y', 'z', '0', '1', '2', '3', // 6
            '4', '5', '6', '7', '8', '9', '+', '/', // 7
            // 不够的用‘=’填充
            '=' };
    private static final byte[] pem_conver = new byte[128];

    static {
        Arrays.fill(pem_conver, (byte) -1);
        for (int i = 0; i < pem_array.length; i++) {
            pem_conver[pem_array[i]] = (byte) i;
        }
    }

    /**
     * 加密
     */
    public static byte[] encode(byte[] data) {
        Assert.notNull(data,"encode bytes can not null");
        int length = (data.length + 2) / 3 * 4;
        byte[] mm = new byte[length];

        for (int i = 0, j = 0; i < data.length; i += 3, j += 4) {
            boolean isOne = false;
            boolean isTwo = false;
            int val = (data[i] & 0xff) << 8;
            if (i + 1 < data.length) {
                val |= data[i + 1] & 0xff;
                isOne = true;
            }
            val <<= 8;
            if (i + 2 < data.length) {
                val |= data[i + 2] & 0xff;
                isTwo = true;
            }
            mm[j + 3] = pem_array[isTwo ? val & 0x3f : pem_array.length - 1];
            mm[j + 2] = pem_array[isOne ? (val >> 6) & 0x3f : pem_array.length - 1];
            mm[j + 1] = pem_array[(val >> 12) & 0x3f];
            mm[j] = pem_array[(val >> 18) & 0x3f];
        }

        return mm;
    }

    public static String encodeToString(byte[] data){
        return new String(encode(data));
    }

    /**
     * 解密
     */
    public static byte[] decode(byte[] data) {
        Assert.isTrue(!ObjectUtils.isEmpty(data) && data.length % 4 == 0,
                "this bytes can not decode");
        for(int i=0;i<data.length;i++){
            Assert.isTrue(pem_conver[data[i]]!=-1,"the "+i+" character is error:"+data[i]);
        }
        // place为'='
        byte place = pem_array[pem_array.length - 1];
        int length = data.length / 4 * 3;
        if (data[data.length - 1] == place)
            length--;
        if (data[data.length - 2] == place)
            length--;
        byte[] mw = new byte[length];

        for (int i = 0, j = 0; i < data.length; i += 4, j += 3) {
            int val = (pem_conver[data[i]] & 0x3f) << 18
                    | (pem_conver[data[i + 1]] & 0x3f) << 12
                    | (pem_conver[data[i + 2]] & 0x3f) << 6
                    | (pem_conver[data[i + 3]] & 0x3f);
            if (data[i + 3] != place) {
                mw[j + 2] = (byte) (val & 0xff);
            }
            val >>= 8;
            if (data[i + 2] != place) {
                mw[j + 1] = (byte) (val & 0xff);
            }
            val >>= 8;
            mw[j] = (byte) (val & 0xff);
        }

        return mw;
    }

    public static byte[] decodeFromString(String base64String){
        return decode(base64String.getBytes());
    }

}


