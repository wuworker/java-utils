package com.wxl.utils.file.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Map;

/**
 * Created by wuxingle on 2017/11/27.
 * excel导出参数设置
 */
public class ExportConfigure {

    /**
     * 设置合并的单元格
     */
    public CellRangeAddress[] getMergeRange(){
        return null;
    }

    /**
     * 行高
     * key 第几行
     * valye 高度(磅)
     */
    public Map<Integer, Integer> getRowHeight() {
        return null;
    }

    /**
     * 列宽
     * key 第几列
     * valye 字符数
     */
    public Map<Integer, Integer> getColumnCharNum() {
        return null;
    }

    /**
     * 数据体风格设置
     */
    public void configureBodyStyle(Font font, CellStyle cellStyle) {

    }

}



