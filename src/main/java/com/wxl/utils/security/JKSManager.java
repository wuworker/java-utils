package com.wxl.utils.security;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Base64;

/**
 * Created by wuxingle on 2018/05/09
 * jks管理
 */
public class JKSManager {


    /**
     * 从keystore中获取private key
     *
     * @param jksPath   keystore文件路径
     * @param ksPass    keystore密码
     * @param alias     获取key的别名
     * @param aliasPass 获取key密码
     */
    public static PrivateKey getPrivateKey(String jksPath, char[] ksPass, String alias, char[] aliasPass)
            throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return getPrivateKey(getKeyStore(jksPath, ksPass), alias, aliasPass);
    }

    public static PrivateKey getPrivateKey(InputStream in, char[] ksPass, String alias, char[] aliasPass)
            throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return getPrivateKey(getKeyStore(in, ksPass), alias, aliasPass);
    }

    private static PrivateKey getPrivateKey(KeyStore ks, String alias, char[] aliasPass)
            throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        try {
            return (PrivateKey) ks.getKey(alias, aliasPass);
        } catch (KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 导出私钥文件
     */
    public static void genPrivateKeyFile(String fileName, PrivateKey privateKey) throws IOException {
        genPrivateKeyFile(new FileOutputStream(fileName), privateKey.getEncoded());
    }

    public static void genPrivateKeyFile(OutputStream out, PrivateKey privateKey) throws IOException {
        genPrivateKeyFile(out, privateKey.getEncoded());
    }

    public static void genPrivateKeyFile(String fileName, byte[] privateKey) throws IOException {
        genPrivateKeyFile(new FileOutputStream(fileName), privateKey);
    }

    public static void genPrivateKeyFile(OutputStream out, byte[] privateKey) throws IOException {
        outputKey(out, privateKey, "-----BEGIN RSA PRIVATE KEY-----".getBytes(), "-----END RSA PRIVATE KEY-----".getBytes());
    }

    /**
     * 从keystore中获取public key
     *
     * @param jksPath keystore文件路径
     * @param ksPass  keystore密码
     * @param alias   获取key的别名
     */
    public static PublicKey getPublicKey(String jksPath, char[] ksPass, String alias)
            throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return getPublicKey(getKeyStore(jksPath, ksPass), alias);
    }

    public static PublicKey getPublicKey(InputStream in, char[] ksPass, String alias)
            throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return getPublicKey(getKeyStore(in, ksPass), alias);
    }

    private static PublicKey getPublicKey(KeyStore ks, String alias)
            throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        Certificate cer = getCertificate(ks, alias);
        if (cer == null) {
            return null;
        }
        return cer.getPublicKey();
    }

    /**
     * 导出公钥文件
     */
    public static void genPublicKeyFile(String fileName, PublicKey key) throws IOException {
        genPublicKeyFile(new FileOutputStream(fileName), key.getEncoded());
    }

    public static void genPublicKeyFile(OutputStream out, PublicKey key) throws IOException {
        genPublicKeyFile(out, key.getEncoded());
    }

    public static void genPublicKeyFile(String fileName, byte[] key) throws IOException {
        genPublicKeyFile(new FileOutputStream(fileName), key);
    }

    public static void genPublicKeyFile(OutputStream out, byte[] key) throws IOException {
        outputKey(out, key, "-----BEGIN RSA PUBLIC KEY-----".getBytes(), "-----END RSA PUBLIC KEY-----".getBytes());
    }

    /**
     * 从keystore中获取证书
     *
     * @param jksPath keystore文件路径
     * @param ksPass  keystore密码
     * @param alias   获取key的别名
     */
    public static Certificate getCertificate(String jksPath, char[] ksPass, String alias)
            throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return getCertificate(getKeyStore(jksPath, ksPass), alias);
    }

    public static Certificate getCertificate(InputStream in, char[] ksPass, String alias)
            throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return getCertificate(getKeyStore(in, ksPass), alias);
    }

    private static Certificate getCertificate(KeyStore ks, String alias)
            throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        try {
            return ks.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 导出证书
     */
    public static void genCertificateFile(String fileName, Certificate key) throws IOException, CertificateEncodingException {
        genCertificateFile(new FileOutputStream(fileName), key.getEncoded());
    }

    public static void genCertificateFile(OutputStream out, Certificate key) throws IOException, CertificateEncodingException {
        genCertificateFile(out, key.getEncoded());
    }

    public static void genCertificateFile(String fileName, byte[] key) throws IOException {
        genCertificateFile(new FileOutputStream(fileName), key);
    }

    public static void genCertificateFile(OutputStream out, byte[] key) throws IOException {
        outputKey(out, key, "-----BEGIN CERTIFICATE-----".getBytes(), "-----END CERTIFICATE-----".getBytes());
    }


    /**
     * 获取keystore
     *
     * @param path keystore文件路径
     * @param pass keystore密码
     */
    private static KeyStore getKeyStore(String path, char[] pass) throws IOException, CertificateException {
        return getKeyStore(new FileInputStream(path), pass);
    }

    private static KeyStore getKeyStore(InputStream in, char[] pass) throws IOException, CertificateException {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(in, pass);
            return keyStore;
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException(e);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 把key以BASE64输出
     *
     * @param key key的二进制数据
     */
    private static void outputKey(OutputStream out, byte[] key, byte[] start, byte[] end) throws IOException {
        //\r\n
        byte[] newLine = {13, 10};
        try {
            out.write(start);
            out.write(newLine);
            out.write(Base64.getMimeEncoder().encode(key));
            out.write(newLine);
            out.write(end);
        } finally {
            out.close();
        }
    }

}
