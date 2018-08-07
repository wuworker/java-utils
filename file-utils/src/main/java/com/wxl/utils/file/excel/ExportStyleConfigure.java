package com.wxl.utils.file.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

/**
 * Created by wuxingle on 2017/11/27.
 * excel导出风格设置
 */
public interface ExportStyleConfigure {

    void configureStyle(Font font, CellStyle cellStyle);

}

