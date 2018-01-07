package com.wxl.utils.annotation;

import java.lang.annotation.*;

/**
 * Created by wuxingle on 2017/12/2 0002.
 * 被这个注解标记说明线程安全
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ThreadSafe {
}
