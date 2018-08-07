package com.wxl.utils.base;

import org.springframework.util.Assert;

/**
 * Created by wuxingle on 2017/9/7 0007.
 * 数据相关工具类
 */
public class DataUtils {

    /**
     * byte转16进制字符串
     */
    public static String toHex(byte[] bytes) {
        Assert.notNull(bytes,"toHex bytes can not null");
        char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(chars[(b & 0xff) >> 4]);
            sb.append(chars[(b & 0x0f)]);
        }
        return sb.toString();
    }

    /**
     * 16进制字符串转byte
     */
    public static byte[] toByte(String hex) {
        Assert.hasText(hex,"toByte hexString can not empty");
        Assert.isTrue(hex.length() % 2 == 0,"hexString must is even numbers");
        char[] charArrays = hex.toUpperCase().toCharArray();
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (toByteFromHexchar(charArrays[i * 2]) << 4 | toByteFromHexchar(charArrays[i * 2 + 1]));
        }
        return result;
    }

    private static byte toByteFromHexchar(char c) {
        int b = "0123456789ABCDEF".indexOf(c);
        Assert.isTrue(b>=0,"hexString can only contain [0-9a-fA-F]");
        return (byte)b;
    }


}
