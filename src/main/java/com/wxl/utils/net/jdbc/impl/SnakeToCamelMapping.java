package com.wxl.utils.net.jdbc.impl;

import com.wxl.utils.AliasUtil;
import com.wxl.utils.net.jdbc.JdbcMapping;

/**
 * Created by wuxingle on 2017/11/24.
 * 蛇形变量转驼峰变量
 */
public class SnakeToCamelMapping implements JdbcMapping{

    /**
     * @param name 数据库字段
     * @return 实体类字段
     */
    @Override
    public String mapping(String name) {
        return AliasUtil.snakeToCamel(name);
    }


}
