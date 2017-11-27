package com.wxl.utils.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;

/**
 * Created by wuxingle on 2017/11/27.
 * 头处理相关类生成器
 */
public interface ExportHeadGenerator {

    Font generateFont();

    CellStyle generateCellStyle();

    Row generateRow(int rowIndex);

}
