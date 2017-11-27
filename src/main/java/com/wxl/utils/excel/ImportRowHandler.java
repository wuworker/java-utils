package com.wxl.utils.excel;

import java.util.Map;

/**
 * Created by wuxingle on 2017/11/27.
 * excel导入行处理
 */
public interface ImportRowHandler<T> {

    /**
     * @param rows 行数据
     *             key   列号
     *             vale  数据
     * @return 返回行处理结果
     */
    T doWithRow(Map<Integer, String> rows);
}
