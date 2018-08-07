package com.wxl.utils.file;


import com.wxl.utils.base.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

/**
 * Created by wuxingle on 2018/05/28
 * 文件压缩解压缩(zip,gzip)
 * gzip压缩，调用close方法还会写入byte,所以如果需要byte,应该调用close后获取，否则会丢失数据
 */
public class CompressUtils {

    //----------------------------zip------------------------------------

    /**
     * 多文件压缩
     */
    public static byte[] zipToByte(File... files) throws IOException {
        if (files.length == 0) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        zipAndClose(out, files);
        return out.toByteArray();
    }

    public static void zip(File zipFile, File... files) throws IOException {
        zipAndClose(new FileOutputStream(zipFile), files);
    }

    public static void zip(OutputStream out, File... files) throws IOException {
        zip(new ZipOutputStream(out), files);
    }

    public static void zip(ZipOutputStream out, File... files) throws IOException {
        for (File file : files) {
            zipFile(out, file, "");
        }
    }

    public static void zipAndClose(OutputStream out, File... files) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
            zip(zipOut, files);
        }
    }

    /**
     * 从URL下载进行压缩
     */
    public static byte[] zipToByte(URL... urls) throws IOException {
        if (urls.length == 0) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        zipAndClose(out, urls);
        return out.toByteArray();
    }

    public static void zip(File zipFile, URL... urls) throws IOException {
        zipAndClose(new FileOutputStream(zipFile), urls);
    }

    public static void zip(OutputStream out, URL... urls) throws IOException {
        zip(new ZipOutputStream(out), urls);
    }

    public static void zip(ZipOutputStream out, URL... urls) throws IOException {
        for (URL url : urls) {
            String file = url.getPath();
            int index = file.lastIndexOf("/");
            String name = index != -1 && index < file.length() - 1 ? file.substring(index + 1) : file;
            out.putNextEntry(new ZipEntry(name));
            IOUtils.copy(url.openStream(), out, true, false);
        }
    }

    public static void zipAndClose(OutputStream out, URL... urls) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
            zip(zipOut, urls);
        }
    }

    /**
     * 文件递归压缩
     */
    private static void zipFile(ZipOutputStream zipOut, File source, String base) throws IOException {
        if (base == null) {
            base = "";
        }
        if (source.isDirectory()) {
            File[] files = source.listFiles();
            if (files == null) {
                throw new IllegalStateException("file path is isInvalid:" + source);
            }
            zipOut.putNextEntry(new ZipEntry(base + getFileName(source) + "/"));
            for (File file : files) {
                zipFile(zipOut, file, base + getFileName(source) + "/");
            }
        } else {
            zipOut.putNextEntry(new ZipEntry(base + getFileName(source)));
            IOUtils.copy(new FileInputStream(source), zipOut, true, false);
        }
    }

    private static String getFileName(File file) {
        String name = file.getName();
        if (file.isFile()) {
            return name.lastIndexOf("/") > 0 ? name.substring(name.lastIndexOf("/") + 1) : name;
        } else {
            if (name.lastIndexOf("/") == name.length() - 1) {
                return name;
            }
            return name.substring(name.lastIndexOf("/") + 1);
        }
    }

    /**
     * 解压成文件
     *
     * @param outPath 输出路径
     * @param replace 如果文件存在是否替换
     */
    public static void unzip(File zipFile, String outPath, boolean replace) throws IOException {
        unzipAndClose(new FileInputStream(zipFile), outPath, replace);
    }

    public static void unzip(URL url, String outPath, boolean replace) throws IOException {
        unzipAndClose(url.openStream(), outPath, replace);
    }

    public static void unzip(ZipInputStream zipIn, String outPath, boolean replace) throws IOException {
        ZipEntry entry;
        while ((entry = zipIn.getNextEntry()) != null) {
            Path path = Paths.get(outPath, entry.getName());
            if (entry.isDirectory()) {
                if (Files.notExists(path)) {
                    Files.createDirectories(path);
                }
            } else {
                if (replace) {
                    Files.copy(zipIn, path, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.copy(zipIn, path);
                }
            }
        }
    }

    public static void unzipAndClose(InputStream in, String outPath, boolean replace) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(in)) {
            unzip(zipIn, outPath, replace);
        }
    }

    /**
     * 解压出文件内容保存为byte
     */
    public static List<byte[]> unzip(File zipFile) throws IOException {
        return unzipAndClose(new FileInputStream(zipFile));
    }

    public static List<byte[]> unzip(URL url) throws IOException {
        return unzipAndClose(url.openStream());
    }

    public static List<byte[]> unzip(InputStream in) throws IOException {
        return unzip(new ZipInputStream(in));
    }

    public static List<byte[]> unzip(ZipInputStream in) throws IOException {
        List<byte[]> list = new ArrayList<>();
        ZipEntry entry;
        while ((entry = in.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            IOUtils.copy(in, byteOut, false, true);
            list.add(byteOut.toByteArray());
        }
        return list;
    }

    public static List<byte[]> unzipAndClose(InputStream in) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(in)) {
            return unzip(zipIn);
        }
    }

    //----------------------------gzip------------------------------------

    /**
     * 压缩
     */
    public static byte[] gzip(byte[] bytes) throws IOException {
        return gzipAndClose(new ByteArrayInputStream(bytes));
    }

    public static byte[] gzip(File file) throws IOException {
        return gzipAndClose(new FileInputStream(file));
    }

    public static byte[] gzip(URL url) throws IOException {
        return gzipAndClose(url.openStream());
    }

    public static byte[] gzip(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, new GZIPOutputStream(out), false, true);
        return out.toByteArray();
    }

    public static byte[] gzipAndClose(InputStream in) throws IOException {
        try {
            return gzip(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 从输入流压缩到输出流
     */
    public static void gzip(InputStream in, OutputStream out) throws IOException {
        IOUtils.copy(in, new GZIPOutputStream(out));
    }

    public static void gzip(InputStream in, OutputStream out, boolean closeIn, boolean closeOut) throws IOException {
        IOUtils.copy(in, new GZIPOutputStream(out), closeIn, closeOut);
    }


    /**
     * 解压
     */
    public static byte[] ungzip(byte[] bytes) throws IOException {
        return ungzipAndClose(new ByteArrayInputStream(bytes));
    }

    public static byte[] ungzip(File file) throws IOException {
        return ungzipAndClose(new FileInputStream(file));
    }

    public static byte[] ungzip(URL url) throws IOException {
        return ungzipAndClose(url.openStream());
    }

    public static byte[] ungzip(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(new GZIPInputStream(in), out, false, true);

        return out.toByteArray();
    }

    public static byte[] ungzipAndClose(InputStream in) throws IOException {
        try {
            return ungzip(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 从输入流解压到输出流
     */
    public static void ungzip(InputStream in, OutputStream out) throws IOException {
        IOUtils.copy(new GZIPInputStream(in), out);
    }

    public static void ungzip(InputStream in, OutputStream out, boolean closeIn, boolean closeOut) throws IOException {
        IOUtils.copy(new GZIPInputStream(in), out, closeIn, closeOut);
    }


}












