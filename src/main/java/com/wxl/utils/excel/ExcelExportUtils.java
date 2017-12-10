package com.wxl.utils.excel;

import com.wxl.utils.excel.prop.ExcelVersion;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

import static com.wxl.utils.ReflectUtils.getObjectValue;

/**
 * Created by wuxingle on 2017/10/21 0021.
 * excel导出工具类
 */
public class ExcelExportUtils {

    /**
     * 根据版本获取workbook
     */
    public static Workbook createTmp(ExcelVersion version) {
        Assert.notNull(version, "version can not null");
        switch (version) {
            case XLS:
                return new HSSFWorkbook();
            case XLSX:
                return new XSSFWorkbook();
            default:
                throw new IllegalArgumentException("version unknow,muse be [XLS/XLSX]");
        }
    }

    /**
     * 导出
     */
    public static <T> void export(List<T> list, OutputStream out, ExcelVersion version) throws IOException {
        export(list, out, null, null, version);
    }

    public static <T> void exportToTmp(Workbook workbook, List<T> list) {
        exportToTmp(workbook, list, null, null);
    }

    public static <T> void export(List<T> list, OutputStream out, String sheetName,
                                  String[] titles, ExcelVersion version) throws IOException {
        export(list, out, sheetName, titles, null, version);
    }

    public static <T> void exportToTmp(Workbook workbook, List<T> list, String sheetName, String[] titles) {
        exportToTmp(workbook, list, sheetName, titles, null);
    }


    public static <T> void export(List<T> list, OutputStream out, String sheetName, String[] titles,
                                  ExportStyleConfigure titleConfig, ExcelVersion version) throws IOException {
        export(list, out, sheetName, titles, titleConfig, new ExportConfigure(), version);
    }

    public static <T> void exportToTmp(Workbook workbook, List<T> list, String sheetName, String[] titles,
                                       ExportStyleConfigure titleConfig) {
        exportToTmp(workbook, list, sheetName, titles, titleConfig, new ExportConfigure());
    }


    public static <T> void export(List<T> list, OutputStream out, String sheetName, String[] titles,
                                  ExportStyleConfigure titleConfig, final ExportStyleConfigure bodyConfig,
                                  ExcelVersion version) throws IOException {
        export(list, out, sheetName, titles, titleConfig, new ExportConfigure() {
            @Override
            public void configureBodyStyle(Font font, CellStyle cellStyle) {
                if (bodyConfig != null) {
                    bodyConfig.configureStyle(font, cellStyle);
                }
            }
        }, version);
    }

    public static <T> void exportToTmp(Workbook workbook, List<T> list, String sheetName, String[] titles,
                                       ExportStyleConfigure titleConfig, final ExportStyleConfigure bodyConfig) {
        exportToTmp(workbook, list, sheetName, titles, titleConfig, new ExportConfigure() {
            @Override
            public void configureBodyStyle(Font font, CellStyle cellStyle) {
                if (bodyConfig != null) {
                    bodyConfig.configureStyle(font, cellStyle);
                }
            }
        });
    }

    public static <T> void export(List<T> list, OutputStream out, String sheetName, String[] titles,
                                  ExportStyleConfigure titleConfig, ExportConfigure configure,
                                  ExcelVersion version) throws IOException {
        try {
            Workbook workbook = createTmp(version);
            exportToTmp(workbook, list, sheetName, titles, titleConfig, configure);
            outputTmp(workbook, out);
        } finally {
            closeOutput(out);
        }
    }

    public static <T> void exportToTmp(Workbook workbook, List<T> list, String sheetName, final String[] titles,
                                       final ExportStyleConfigure titleConfig, ExportConfigure configure) {
        exportToTmp(workbook, list, sheetName, (h) -> {
            if (ObjectUtils.isEmpty(titles)) {
                return 0;
            }
            Row row = h.generateRow(0);
            Font font = h.generateFont();
            CellStyle cellStyle = h.generateCellStyle();
            cellStyle.setFont(font);
            if (titleConfig != null) {
                titleConfig.configureStyle(font, cellStyle);
            }
            for (int i = 0; i < titles.length; i++) {
                Cell cell = row.createCell(i);
                setCellValue(cell, titles[i]);
                cell.setCellStyle(cellStyle);
            }
            return 1;
        }, configure);
    }

    /**
     * 导出一张表
     *
     * @param list        数据列表
     * @param out         输出流
     * @param sheetName   表名
     * @param headHandler 头处理工具
     * @param configure   参数设置
     * @param version     版本
     */
    public static <T> void export(List<T> list, OutputStream out, String sheetName, ExportHeadHandler headHandler,
                                  ExportConfigure configure, ExcelVersion version) throws IOException {
        try {
            Workbook workbook = createTmp(version);
            exportToTmp(workbook, list, sheetName, headHandler, configure);
            outputTmp(workbook, out);
        } finally {
            closeOutput(out);
        }
    }


    /**
     * 把数据写入workbook
     */
    public static <T> void exportToTmp(final Workbook workbook, List<T> list, String sheetName,
                                       ExportHeadHandler headHandler, ExportConfigure configure) {
        Assert.notNull(workbook, "workbook can not null");
        Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(sheetName));
        int start = 0;
        //头处理
        if (headHandler != null) {
            ExportHeadGenerator headGenerator = new ExportHeadGenerator() {
                //默认风格
                @Override
                public Font generateFont() {
                    Font font = workbook.createFont();
                    font.setFontName("宋体");
                    font.setBold(true);
                    font.setFontHeightInPoints((short) 14);
                    return font;
                }

                @Override
                public CellStyle generateCellStyle() {
                    CellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.setAlignment(HorizontalAlignment.CENTER);
                    cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    return cellStyle;
                }

                @Override
                public Row generateRow(int rowIndex) {
                    return sheet.createRow(rowIndex);
                }
            };
            start = headHandler.handle(headGenerator);
        }

        //数据体风格
        CellStyle bodyStyle = workbook.createCellStyle();
        Font bodyFont = workbook.createFont();
        bodyFont.setFontHeightInPoints((short) 11);
        bodyFont.setFontName("宋体");
        bodyStyle.setAlignment(HorizontalAlignment.CENTER);
        bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        bodyStyle.setFont(bodyFont);

        if (configure != null) {
            configure.configureBodyStyle(bodyFont, bodyStyle);
            bodyStyle.setFont(bodyFont);
        }
        List<List<Object>> values = getValuesFromList(list);
        //设置数据
        for (int i = 0; i < values.size(); i++) {
            Row row = sheet.createRow(start + i);
            int columnSize = values.get(i).size();
            for (int j = 0; j < columnSize; j++) {
                Cell bodyCell = row.createCell(j);
                bodyCell.setCellStyle(bodyStyle);
                Object value = values.get(i).get(j);
                setCellValue(bodyCell, value);
                //前后留10字符
                setSafeColumnWidth(sheet, j, value.toString().getBytes().length + 10);
            }
        }
        if (configure != null) {
            //合并单元格
            if (!ObjectUtils.isEmpty(configure.getMergeRange())) {
                for (CellRangeAddress address : configure.getMergeRange()) {
                    sheet.addMergedRegion(address);
                }
            }
            //设置行高
            Map<Integer, Integer> rowMap = configure.getRowHeight();
            if (!CollectionUtils.isEmpty(rowMap)) {
                for (Integer index : rowMap.keySet()) {
                    int rowHight = rowMap.get(index);
                    Assert.isTrue(rowHight > 0, "row height must > 0");
                    setRowHeight(sheet.getRow(index), rowHight);
                }
            }
            //设置列宽
            Map<Integer, Integer> columnMap = configure.getColumnCharNum();
            if (!CollectionUtils.isEmpty(columnMap)) {
                for (Integer index : columnMap.keySet()) {
                    int columnWidth = columnMap.get(index);
                    Assert.isTrue(columnWidth > 0, "column Width must > 0");
                    setColumnWidth(sheet, index, columnWidth);
                }
            }
        }
    }

    /**
     * 导出并关闭
     */
    public static void outputTmpAndClose(Workbook workbook, OutputStream out) throws IOException {
        try {
            outputTmp(workbook, out);
        } finally {
            closeOutput(out);
        }
    }

    /**
     * 导出
     */
    public static void outputTmp(Workbook workbook, OutputStream out) throws IOException {
        Assert.notNull(workbook, "workbook can not null");
        Assert.notNull(out, "outputStream can not null");
        workbook.write(out);
    }

    /**
     * 关闭输出流
     */
    public static void closeOutput(OutputStream out) throws IOException {
        if (out != null) {
            out.close();
        }
    }


    /**
     * 设置行高
     *
     * @param height 磅
     */
    private static void setRowHeight(Row row, int height) {
        row.setHeightInPoints(height);
    }

    /**
     * 设置列宽
     *
     * @param charNum 字符数
     */
    private static void setColumnWidth(Sheet sheet, int index, int charNum) {
        sheet.setColumnWidth(index, charNum * 256);
    }

    /**
     * 设置列宽
     * 如果比原来的小不设置
     */
    private static boolean setSafeColumnWidth(Sheet sheet, int index, int charNum) {
        int oldLen = sheet.getColumnWidth(index);
        if (oldLen < charNum * 256) {
            setColumnWidth(sheet, index, charNum);
            return true;
        }
        return false;
    }

    /**
     * 获取属性值
     */
    private static <T> List<List<Object>> getValuesFromList(List<T> list) {
        Assert.notEmpty(list, "input list data cannot empty");
        List<List<Object>> values = new ArrayList<>();
        for (T t : list) {
            if (t == null) {
                values.add(new ArrayList<>());
                continue;
            }
            List<Object> value = new ArrayList<>();
            if (t instanceof Map) {
                Map map = (Map) t;
                for (Object key : map.keySet()) {
                    Object obj = map.get(key);
                    value.add(obj == null ? "" : obj);
                }
            } else if (t instanceof Iterable) {
                Iterable iterable = (Iterable) t;
                for (Object obj : iterable) {
                    value.add(obj == null ? "" : obj);
                }
            } else if (t instanceof Enumeration) {
                Enumeration it = (Enumeration) t;
                while (it.hasMoreElements()) {
                    Object obj = it.nextElement();
                    value.add(obj == null ? "" : obj);
                }
            } else if (isBaseType(t)) {
                value.add(t);
            } else {
                Field[] fileds = t.getClass().getDeclaredFields();
                for (Field f : fileds) {
                    Object v = getObjectValue(t, f);
                    value.add(v == null ? "" : v);
                }
            }
            values.add(value);
        }
        return values;
    }

    /**
     * 设置单元格的值
     */
    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Byte) {
            cell.setCellValue((byte) value);
        } else if (value instanceof Short) {
            cell.setCellValue((short) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((int) value);
        } else if (value instanceof Float) {
            cell.setCellValue((float) value);
        } else if (value instanceof Double) {
            cell.setCellValue((double) value);
        } else if (value instanceof Long) {
            cell.setCellValue((long) value);
        } else if (value instanceof Character) {
            cell.setCellValue((char) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 基本类型
     */
    private static boolean isBaseType(Object obj) {
        Assert.notNull(obj, "object can not null");
        return obj.getClass() == String.class
                || obj.getClass() == Boolean.class
                || (obj instanceof Number);
    }


}
