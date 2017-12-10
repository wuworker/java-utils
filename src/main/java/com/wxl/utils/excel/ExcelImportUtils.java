package com.wxl.utils.excel;

import com.wxl.utils.ReflectUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by wuxingle on 2017/10/21 0021.
 * excel导入工具类
 */
public class ExcelImportUtils {

    /**
     * 获取workbook
     */
    public static Workbook createTmp(InputStream in) throws IOException {
        Assert.notNull(in, "input stream can not null");
        try {
            return WorkbookFactory.create(in);
        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException("create excel workbook error", e);
        }
    }

    /**
     * excel导入成list类型
     */
    public static List<List<String>> imports(InputStream in) throws IOException {
        return imports(in, 0, 0);
    }

    public static List<List<String>> imports(InputStream in, int rowStart) throws IOException {
        return imports(in, 0, rowStart);
    }

    public static List<List<String>> imports(InputStream in, int sheetIndex, int rowStart) throws IOException {
        return imports(in, sheetIndex, rowStart, (map) -> {
            return new ArrayList<>(map.values());
        });
    }

    public static List<List<String>> importFromTmp(Workbook workbook, int sheetIndex) {
        return importFromTmp(workbook, sheetIndex, 0);
    }

    public static List<List<String>> importFromTmp(Workbook workbook, int sheetIndex, int rowStart) {
        return importFromTmp(workbook, sheetIndex, rowStart, (map) -> {
            return new ArrayList<>(map.values());
        });
    }

    /**
     * excel导入成java类
     * 默认第0个表
     *
     * @param clazz 对应的类
     */
    public static <T> List<T> imports(InputStream in, Class<T> clazz, int rowStart) throws IOException {
        return imports(in, clazz, 0, rowStart);
    }

    public static <T> List<T> imports(InputStream in, Class<T> clazz, int sheetIndex, int rowStart) throws IOException {
        try {
            Workbook workbook = createTmp(in);
            return importFromTmp(workbook, clazz, sheetIndex, rowStart);
        } finally {
            closeInput(in);
        }
    }

    public static <T> List<T> importFromTmp(Workbook workbook, Class<T> clazz, int sheetIndex, int rowStart) {
        Assert.notNull(clazz, "param class can not null");
        return importFromTmp(workbook, sheetIndex, rowStart, (map) -> {
            try {
                List<String> list = new ArrayList<>(map.values());
                T t = clazz.newInstance();
                Field[] fields = clazz.getDeclaredFields();
                for (int i = 0; i < fields.length && i < list.size(); i++) {
                    setFieldValue(t, fields[i], list.get(i));
                }
                return t;
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("can not access default construct", e);
            } catch (InstantiationException e) {
                throw new IllegalStateException("create default construct error", e);
            }
        });
    }

    /**
     * excel导入
     *
     * @param in         输入流
     * @param sheetIndex 表索引，小于0则导入所有表
     * @param rowStart   开始读取的行号，从0开始
     * @param rowHandler 行处理，返回行处理后结果
     * @param <T>        行处理结果
     */
    public static <T> List<T> imports(InputStream in, int sheetIndex, int rowStart,
                                      ImportRowHandler<T> rowHandler) throws IOException {
        try {
            Workbook workbook = createTmp(in);
            return importFromTmp(workbook, sheetIndex, rowStart, rowHandler);
        } finally {
            closeInput(in);
        }
    }

    public static <T> List<T> importFromTmp(Workbook workbook, int sheetIndex, int rowStart, ImportRowHandler<T> rowHandler) {
        Assert.notNull(workbook, "workbook can not null");
        Assert.isTrue(sheetIndex >= 0, "sheetIndex must >=0");
        Assert.isTrue(rowStart >= 0, "rowStart must >=0");
        Assert.notNull(rowHandler, "rowHandler can not null");

        List<T> result = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        Assert.notNull(sheet, "can not find sheet by sheetIndex:" + sheetIndex);
        List<Map<Integer, String>> values = getValues(sheet);
        for (int j = rowStart; j < values.size(); j++) {
            result.add(rowHandler.doWithRow(values.get(j)));
        }
        return result;
    }


    /**
     * 关闭输入流
     */
    public static void closeInput(InputStream in) throws IOException {
        Assert.notNull(in, "inputStream can not null");
        in.close();
    }


    /**
     * 获取表的所有字段
     */
    private static List<Map<Integer, String>> getValues(Sheet sheet) {
        List<Map<Integer, String>> values = new ArrayList<>();
        for (Row row : sheet) {
            Map<Integer, String> cells = new LinkedHashMap<>();
            for (Cell cell : row) {
                cells.put(cell.getColumnIndex(), getCellString(cell));
            }
            values.add(cells);
        }
        return values;
    }

    /**
     * 设置属性
     */
    private static void setFieldValue(Object obj, Field field, String cellValue) {
        Class<?> clazz = field.getType();
        Object value = null;
        if (cellValue == null) {
            value = clazz == String.class ? "" : null;
        } else if (clazz == String.class) {
            value = cellValue;
        } else if (clazz == Byte.class || clazz == byte.class) {
            value = Byte.parseByte(cellValue);
        } else if (clazz == Short.class || clazz == short.class) {
            value = Short.parseShort(cellValue);
        } else if (clazz == Character.class || clazz == char.class) {
            value = (char) Integer.parseInt(cellValue);
        } else if (clazz == Integer.class || clazz == int.class) {
            value = Integer.parseInt(cellValue);
        } else if (clazz == Float.class || clazz == float.class) {
            value = Float.parseFloat(cellValue);
        } else if (clazz == Double.class || clazz == double.class) {
            value = Double.parseDouble(cellValue);
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            value = Boolean.parseBoolean(cellValue);
        } else {
            throw new IllegalStateException("can not cast field type:" + clazz.getName());
        }
        ReflectUtils.setObjectValue(obj, field, value);
    }

    /**
     * 获取cell的值
     */
    private static String getCellString(Cell cell) {
        switch (cell.getCellTypeEnum()) {
            case BLANK:
                return null;
            case NUMERIC:
                double num = cell.getNumericCellValue();
                return new BigDecimal(num).toString();
            case STRING:
                return cell.getStringCellValue();
            case ERROR:
                throw new IllegalStateException("read cell value error:" + cell.getAddress().toString());
            default:
                return cell.toString();
        }
    }


}
