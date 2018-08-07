package com.wxl.utils.jdbc;

import java.lang.annotation.*;

/**
 * Created by wuxingle on 2018/03/08
 * 被这个注解标记的类或方法为事务方法
 */
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface IsTransaction {
}
