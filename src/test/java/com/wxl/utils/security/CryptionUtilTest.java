package com.wxl.utils.security;

import org.junit.Test;

import static com.wxl.utils.security.CryptionUtil.*;

/**
 * Created by wuxingle on 2017/9/11 0011.
 *
 */
public class CryptionUtilTest {

    @Test
    public void testdoMD5() throws Exception {
        String pass = "123456";
        String hex = toHex(doMD5(pass.getBytes()));
        String base64 = toBase64(doMD5(pass.getBytes()));
        print(hex,base64);
    }

    @Test
    public void testdoSHA1() throws Exception {
        String pass = "123456";
        String hex = toHex(doSHA1(pass.getBytes()));
        String base64 = toBase64(doSHA1(pass.getBytes()));
        print(hex,base64);
    }

    @Test
    public void testdoSHA256() throws Exception {
        String pass = "123456";
        String hex = toHex(doSHA256(pass.getBytes()));
        String base64 = toBase64(doSHA256(pass.getBytes()));
        print(hex,base64);
    }

    @Test
    public void testdoSHA512() throws Exception {
        String pass = "123456";
        String hex = toHex(doSHA512(pass.getBytes()));
        String base64 = toBase64(doSHA512(pass.getBytes()));
        print(hex,base64);
    }

    @Test
    public void testdoMACWithMD5() throws Exception {
        String pass = "123456";
        String key="abc123";
        String hex = toHex(doMACWithMD5(pass.getBytes(),key.getBytes()));
        String base64 = toBase64(doMACWithMD5(pass.getBytes(),key.getBytes()));
        print(hex,base64);
    }

    @Test
    public void testdoMACWithSHA1() throws Exception {
        String pass = "123456";
        String key="abc123";
        String hex = toHex(doMACWithSHA1(pass.getBytes(),key.getBytes()));
        String base64 = toBase64(doMACWithSHA1(pass.getBytes(),key.getBytes()));
        print(hex,base64);
    }

    @Test
    public void testdoMACWithSHA256() throws Exception {
        String pass = "123456";
        String key="abc123";
        String hex = toHex(doMACWithSHA256(pass.getBytes(),key.getBytes()));
        String base64 = toBase64(doMACWithSHA256(pass.getBytes(),key.getBytes()));
        print(hex,base64);
    }

    @Test
    public void testdoMACWithSHA512() throws Exception {
        String pass = "123456";
        String key="abc123";
        String hex = toHex(doMACWithSHA512(pass.getBytes(),key.getBytes()));
        String base64 = toBase64(doMACWithSHA512(pass.getBytes(),key.getBytes()));
        print(hex,base64);
    }

    @Test
    public void testdoAES() throws Exception {
        String pass = "123456";
        byte[] key = KeyGeneratorUtil.generateAESKey();
        System.out.println("密钥长度:"+key.length*8);
        System.out.println(toHex(key));

        String hex = toHex(doAESEncode(pass.getBytes(),key));
        String base64 = toBase64(doAESEncode(pass.getBytes(),key));
        print(hex,base64);

        String source1 = new String(doAESDecode(fromHex(hex),key));
        String source2 = new String(doAESDecode(fromBase64(base64),key));

        System.out.println(source1);
        System.out.println(source2);
    }

    @Test
    public void testdoRSA() throws Exception {
        String pass = "123456";
        byte[][] keyPair = KeyGeneratorUtil.generateRSAKey1024();
        byte[] privateKey = keyPair[0];
        byte[] publicKey = keyPair[1];

        //公钥加密
        String hex = toHex(doRSAPublicKeyEncode(pass.getBytes(),publicKey));
        String base64 = toBase64(doRSAPublicKeyEncode(pass.getBytes(),publicKey));
        print(hex,base64);
        //私钥解密
        String s1 = new String(doRSAPrivateKeyDecode(fromHex(hex),privateKey));
        String s2 = new String(doRSAPrivateKeyDecode(fromBase64(base64),privateKey));
        System.out.println(s1);
        System.out.println(s2);

        System.out.println("-----------------------------------------------");

        //私钥加密
        String hex0 = toHex(doRSAPrivateKeyEncode(pass.getBytes(),privateKey));
        String base640 = toBase64(doRSAPrivateKeyEncode(pass.getBytes(),privateKey));
        print(hex0,base640);
        //公钥解密
        String s10 = new String(doRSAPublicKeyDecode(fromHex(hex0),publicKey));
        String s20 = new String(doRSAPublicKeyDecode(fromBase64(base640),publicKey));
        System.out.println(s10);
        System.out.println(s20);

    }




    private void print(String hex,String base64){
        System.out.println(hex.length() * 4);
        System.out.println(hex);
        System.out.println(base64);
    }
}