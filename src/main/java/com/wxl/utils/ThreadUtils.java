package com.wxl.utils;

/**
 * Created by wuxingle on 2018/1/7 0007.
 * 线程相关工具类
 */
public class ThreadUtils {

    /**
     * 暂停当前线程
     */
    public static void sleep(long ms) throws IllegalStateException {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * 当前线程是否是main线程
     */
    public static boolean isMain() {
        return isMain(Thread.currentThread());
    }

    public static boolean isMain(Thread thread) {
        return thread.getId() == 1;
    }

    /**
     * 获取线程打印信息
     */
    public static String getPrintThreadInfo() {
        return getPrintThreadInfo(Thread.currentThread());
    }

    public static String getPrintThreadInfo(Thread thread) {
        return "thread id:" + thread.getId()
                + ", name:" + thread.getName()
                + ", group:" + thread.getThreadGroup().getName()
                + ", daemon:" + thread.isDaemon()
                + ", priority:" + thread.getPriority()
                + ", state:" + thread.getState().name();
    }


}
