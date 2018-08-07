package com.wxl.utils.base.security;

import com.wxl.utils.base.collection.ByteArrayList;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Base64;

/**
 * Created by wuxingle on 2018/05/09
 */
public class JKSManagerTest {

    /**
     * keytool -genkeypair -alias test1 -keyalg RSA -keystore test.keystore
     * keystore密码 123456
     * test1密码 123456(private)
     * test2密码 test2123456(public)
     */
    @Test
    public void getKeyFromJKS() throws Exception {
        PrivateKey privateKey = JKSManager.getPrivateKey(getKSInputStream(), "123456".toCharArray(), "test1", "123456".toCharArray());
        PublicKey publicKey = JKSManager.getPublicKey(getKSInputStream(), "123456".toCharArray(), "test1");
        Certificate certificate = JKSManager.getCertificate(getKSInputStream(), "123456".toCharArray(), "test1");

        System.out.println(SecurityUtils.toBase64(privateKey.getEncoded()));
        System.out.println();
        System.out.println(SecurityUtils.toBase64(publicKey.getEncoded()));
        System.out.println();
        System.out.println(SecurityUtils.toBase64(certificate.getEncoded()));

        String msg = "hello keytool";
        byte[] encode = SecurityUtils.doRSAPublicKeyEncode(msg.getBytes(), publicKey.getEncoded());
        byte[] decode = SecurityUtils.doRSAPrivateKeyDecode(encode, privateKey.getEncoded());

        System.out.println();
        System.out.println(new String(decode));

        String path = "src/test/resources/com/wxl/utils/base/security";
        JKSManager.genPrivateKeyFile(path + "/rsa_private.key", privateKey.getEncoded());
        JKSManager.genPublicKeyFile(path + "/rsa_public.key", publicKey.getEncoded());
        JKSManager.genCertificateFile(path + "/rsa_cert.key", certificate.getEncoded());

        System.out.println("cer file text:");
        try (FileInputStream in = new FileInputStream(path + "/test1.cer")){
            int len;
            byte[] tmp = new byte[255];
            ByteArrayList bytes = new ByteArrayList();
            while ((len = in.read(tmp)) != -1) {
                bytes.addAll(tmp, 0, len);
            }

            System.out.println(Base64.getMimeEncoder().encodeToString(bytes.toByte()));
        }

    }


    @Test
    public void test2() {
        KeyPair keyPair = KeyGeneratorUtils.generateRSAKey2048();

        String msg = "hello keytool";
        byte[] encode = SecurityUtils.doRSAPublicKeyEncode(msg.getBytes(), keyPair.getPublic().getEncoded());

        byte[] decode = SecurityUtils.doRSAPrivateKeyDecode(encode, keyPair.getPrivate().getEncoded());

        System.out.println(new String(decode));
    }


    private InputStream getKSInputStream() {
        return JKSManagerTest.class.getResourceAsStream("test1.keystore");
    }


}




























