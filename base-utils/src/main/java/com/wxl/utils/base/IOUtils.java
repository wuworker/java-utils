package com.wxl.utils.base;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Create by wuxingle on 2018/6/23
 * io工具类
 */
public class IOUtils {

    private static final int DEFAULT_BUFFER = 4 * 1024;

    /**
     * 输入流转字节
     */
    public static byte[] toByte(InputStream in) throws IOException {
        return toByte(in, DEFAULT_BUFFER);
    }

    public static byte[] toByte(InputStream in, int buffer) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out, buffer, true, true);
        return out.toByteArray();
    }

    public static byte[] toByte(Reader in) throws IOException {
        return toByte(in, DEFAULT_BUFFER);
    }

    public static byte[] toByte(Reader in, int buffer) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, new OutputStreamWriter(out), buffer, true, true);
        return out.toByteArray();
    }

    /**
     * 输入流转字符
     */
    public static char[] toChar(InputStream in, Charset charset) throws IOException {
        return toChar(in, charset, DEFAULT_BUFFER);
    }

    public static char[] toChar(InputStream in, Charset charset, int buffer) throws IOException {
        CharArrayWriter writer = new CharArrayWriter();
        copy(new InputStreamReader(in, charset), writer, buffer, true, true);
        return writer.toCharArray();
    }

    public static char[] toChar(Reader in) throws IOException {
        return toChar(in, DEFAULT_BUFFER);
    }

    public static char[] toChar(Reader in, int buffer) throws IOException {
        CharArrayWriter writer = new CharArrayWriter();
        copy(in, writer, buffer, true, true);
        return writer.toCharArray();
    }

    /**
     * 输入流转字节
     */
    public static String toString(InputStream in, Charset charset) throws IOException {
        return new String(toByte(in), charset);
    }

    public static String toString(InputStream in, Charset charset, int buffer) throws IOException {
        return new String(toByte(in, buffer), charset);
    }

    public static String toString(Reader in) throws IOException {
        return new String(toByte(in));
    }

    public static String toString(Reader in, int buffer) throws IOException {
        return new String(toByte(in, buffer));
    }


    /**
     * 从输入流拷贝到输出流
     * 字节流
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        return copy(in, out, DEFAULT_BUFFER, false, false);
    }

    public static int copy(InputStream in, OutputStream out, int buffer) throws IOException {
        return copy(in, out, buffer, false, false);
    }

    public static int copy(InputStream in, OutputStream out, boolean closeIn, boolean closeOut) throws IOException {
        return copy(in, out, DEFAULT_BUFFER, closeIn, closeOut);
    }

    public static int copy(InputStream in, OutputStream out, int buffer, boolean closeIn, boolean closeOut) throws IOException {
        try {
            byte[] data = new byte[buffer];
            int l;
            long count = 0;
            while ((l = in.read(data)) != -1) {
                out.write(data, 0, l);
                count += l;
                if (count > Integer.MAX_VALUE) {
                    throw new IOException("too large data for input stream!");
                }
            }
            out.flush();
            return (int) count;
        } finally {
            if (closeIn) {
                try {
                    in.close();
                } catch (IOException e) {
                    //ignore
                }
            }
            if (closeOut) {
                try {
                    out.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    /**
     * 从输入流拷贝到输出流
     * 字符流
     */
    public static int copy(Reader in, Writer out) throws IOException {
        return copy(in, out, DEFAULT_BUFFER, false, false);
    }

    public static int copy(Reader in, Writer out, int buffer) throws IOException {
        return copy(in, out, buffer, false, false);
    }

    public static int copy(Reader in, Writer out, boolean closeIn, boolean closeOut) throws IOException {
        return copy(in, out, DEFAULT_BUFFER, closeIn, closeOut);
    }

    public static int copy(Reader in, Writer out, int buffer, boolean closeIn, boolean closeOut) throws IOException {
        try {
            char[] data = new char[buffer];
            int l;
            long count = 0;
            while ((l = in.read(data)) != -1) {
                out.write(data, 0, l);
                count += l;
                if (count > Integer.MAX_VALUE) {
                    throw new IOException("too large data for input stream!");
                }
            }
            out.flush();
            return (int) count;
        } finally {
            if (closeIn) {
                try {
                    in.close();
                } catch (IOException e) {
                    //ignore
                }
            }
            if (closeOut) {
                try {
                    out.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }


}
