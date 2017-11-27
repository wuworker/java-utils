package com.wxl.utils.security;

import com.wxl.utils.DataUtil;
import org.junit.Test;

/**
 * Created by wuxingle on 2017/9/10 0010.
 *
 */
public class KeyGeneratorUtilTest {

    @Test
    public void testGenerateKeyHMAC(){
        //HMAC
        byte[] bytes1 = KeyGeneratorUtil.generateMACWithMD5Key();
        printResult("HMACwithMD5",bytes1);

        byte[] bytes2 = KeyGeneratorUtil.generateMACWithSHA1Key();
        printResult("HMACwithSHA1",bytes2);

        byte[] bytes3 = KeyGeneratorUtil.generateMACWithSHA256Key();
        printResult("HMACwithSHA256",bytes3);

        byte[] bytes4 = KeyGeneratorUtil.generateMACWithSHA512Key();
        printResult("HMACwithSHA512",bytes4);
    }

    @Test
    public void testAesKey(){
        byte[] bytes = KeyGeneratorUtil.generateAESKey();
        printResult("AES",bytes);
    }








    private void printResult(String head,byte[] key){
        System.out.println(head+":");
        System.out.println(key.length * 8);
        System.out.println(DataUtil.toHex(key));
        System.out.println("-------------------------------------");
    }


}