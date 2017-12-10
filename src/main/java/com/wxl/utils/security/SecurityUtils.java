package com.wxl.utils.security;

import com.wxl.utils.DataUtils;
import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by wuxingle on 2017/9/10 0010.
 * 加解密工具类
 */
public class SecurityUtils {

    /**
     * byte转16进制字符串
     */
    public static String toHex(byte[] bytes){
        return DataUtils.toHex(bytes);
    }

    /**
     * 16进制字符串转byte
     */
    public static byte[] fromHex(String hex){
        return DataUtils.toByte(hex);
    }

    /**
     * 转为Base64字符串
     */
    public static String toBase64(byte[] bytes){
        return Base64Utils.encodeToString(bytes);
    }

    /**
     * 从Base64字符串转为byte
     */
    public static byte[] fromBase64(String base64){
        return Base64Utils.decodeFromString(base64);
    }

    /**
     * MD5
     * 128位
     */
    public static byte[] doMD5(byte[] source){
        return encodeFromMessageDigest(source,"md5");
    }

    /**
     * SHA-1
     * 160位
     */
    public static byte[] doSHA1(byte[] source){
        return encodeFromMessageDigest(source,"SHA-1");
    }

    /**
     * SHA-256
     * 256位
     */
    public static byte[] doSHA256(byte[] source){
        return encodeFromMessageDigest(source,"SHA-256");
    }

    /**
     * SHA-512
     * 512位
     */
    public static byte[] doSHA512(byte[] source){
        return encodeFromMessageDigest(source,"SHA-512");
    }


    /**
     * HmacMD5加密
     * @param key 密钥
     */
    public static byte[] doMACWithMD5(byte[] source,byte[] key){
        return encodeFromMAC(source,key,"HmacMD5");
    }


    /**
     * HmacSHA1加密
     * @param key 密钥
     */
    public static byte[] doMACWithSHA1(byte[] source,byte[] key){
        return encodeFromMAC(source,key,"HmacSHA1");
    }


    /**
     * HmacSHA256加密
     * @param key 密钥
     */
    public static byte[] doMACWithSHA256(byte[] source,byte[] key){
        return encodeFromMAC(source,key,"HmacSHA256");
    }


    /**
     * HmacSHA512加密
     * @param key 密钥
     */
    public static byte[] doMACWithSHA512(byte[] source,byte[] key){
        return encodeFromMAC(source,key,"HmacSHA512");
    }


    /**
     * AES加密
     */
    public static byte[] doAESEncode(byte[] source,byte[] key){
        SecretKey secretKey = new SecretKeySpec(key,"AES");
        return encodeFromCipher(source,secretKey,"AES/ECB/PKCS5Padding");
    }

    /**
     * AES解密
     */
    public static byte[] doAESDecode(byte[] source,byte[] key){
        SecretKey secretKey = new SecretKeySpec(key,"AES");
        return decodeFromCipher(source,secretKey,"AES/ECB/PKCS5Padding");
    }


    /**
     * RSA私钥加密
     */
    public static byte[] doRSAPrivateKeyEncode(byte[] source,byte[] key){
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(key);
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

            return encodeFromCipher(source,privateKey,"RSA");
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * RSA公钥加密
     */
    public static byte[] doRSAPublicKeyEncode(byte[] source,byte[] key){
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(key);
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

            return encodeFromCipher(source,publicKey,"RSA");
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    /**
     * RSA私钥解密
     */
    public static byte[] doRSAPrivateKeyDecode(byte[] source,byte[] key){
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(key);
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

            return decodeFromCipher(source,privateKey,"RSA");
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * RSA公钥解密
     */
    public static byte[] doRSAPublicKeyDecode(byte[] source,byte[] key){
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(key);
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

            return decodeFromCipher(source,publicKey,"RSA");
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    /**
     * 消息摘要加密
     * @param source 原文
     * @param algorithm 算法
     */
    private static byte[] encodeFromMessageDigest(byte[] source,String algorithm){
        Assert.notNull(source,"encode source can not null");
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return md.digest(source);
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * MAC加密
     * 含有密钥的消息摘要算法(MAC或HMAC)
     * HmacMD5:128位
     * HmacSHA1:160位...
     */
    private static byte[] encodeFromMAC(byte[] source,byte[] key,String algorithm){
        Assert.notNull(source,"encode source can not null");
        Assert.notNull(key,"key can not null");
        try {
            //根据数据生成密钥
            SecretKey receiveSecretKey = new SecretKeySpec(key, algorithm);
            Mac mac = Mac.getInstance(receiveSecretKey.getAlgorithm());
            mac.init(receiveSecretKey);

            return mac.doFinal(source);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    /**
     * Cipher加密
     */
    private static byte[] encodeFromCipher(byte[] source, Key key, String algorithm){
        Assert.notNull(source,"encode source can not null");
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            //加密
            cipher.init(Cipher.ENCRYPT_MODE,key);
            return cipher.doFinal(source);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Cipher解密
     */
    private static byte[] decodeFromCipher(byte[] source,Key key,String algorithm){
        Assert.notNull(source,"encode source can not null");
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            //解密
            cipher.init(Cipher.DECRYPT_MODE,key);
            return cipher.doFinal(source);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
