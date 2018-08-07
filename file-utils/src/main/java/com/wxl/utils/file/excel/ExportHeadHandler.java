package com.wxl.utils.file.excel;

/**
 * Created by wuxingle on 2017/11/27.
 * excel头处理
 */
public interface ExportHeadHandler {

    /**
     * @return 数据体开始处理的行号
     */
    int handle(ExportHeadGenerator headGenerator);

}

