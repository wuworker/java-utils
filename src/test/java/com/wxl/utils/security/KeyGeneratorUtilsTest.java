package com.wxl.utils.security;

import com.wxl.utils.DataUtils;
import org.junit.Test;

/**
 * Created by wuxingle on 2017/9/10 0010.
 *
 */
public class KeyGeneratorUtilsTest {

    @Test
    public void testGenerateKeyHMAC(){
        //HMAC
        byte[] bytes1 = KeyGeneratorUtils.generateMACWithMD5Key();
        printResult("HMACwithMD5",bytes1);

        byte[] bytes2 = KeyGeneratorUtils.generateMACWithSHA1Key();
        printResult("HMACwithSHA1",bytes2);

        byte[] bytes3 = KeyGeneratorUtils.generateMACWithSHA256Key();
        printResult("HMACwithSHA256",bytes3);

        byte[] bytes4 = KeyGeneratorUtils.generateMACWithSHA512Key();
        printResult("HMACwithSHA512",bytes4);
    }

    @Test
    public void testAesKey(){
        byte[] bytes = KeyGeneratorUtils.generateAESKey();
        printResult("AES",bytes);
    }








    private void printResult(String head,byte[] key){
        System.out.println(head+":");
        System.out.println(key.length * 8);
        System.out.println(DataUtils.toHex(key));
        System.out.println("-------------------------------------");
    }


}