package com.wxl.utils.net.jdbc;

import java.util.Map;

/**
 * Created by wuxingle on 2017/11/24.
 * 记录行处理
 */
public interface JdbcRowHandler<T> {

    /**
     * 处理一行数据
     * @param data 数据
     * @return 处理结果
     */
    T handleRow(Map<String,Object> data);


}
