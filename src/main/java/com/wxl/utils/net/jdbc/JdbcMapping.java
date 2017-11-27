package com.wxl.utils.net.jdbc;

/**
 * Created by wuxingle on 2017/11/24.
 * 数据库字段到实体类的映射
 */
public interface JdbcMapping {

    /**
     * @param name 数据库字段
     * @return 实体类字段
     */
    String mapping(String name);

}
