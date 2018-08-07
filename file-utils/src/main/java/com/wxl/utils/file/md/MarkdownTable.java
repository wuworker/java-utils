package com.wxl.utils.file.md;

import lombok.Data;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by wuxingle on 2018/05/23
 * md表格
 */
@Data
public class MarkdownTable implements MarkdownComponent, Iterable<List<MarkdownComponent>> {

    private static final long serialVersionUID = -6563325369077712196L;

    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;

    //表格
    private List<List<MarkdownComponent>> tables = new ArrayList<>();

    //排列
    private Map<Integer, Integer> aligns = new HashMap<>();

    //默认排列
    private int defaultAlign;

    public MarkdownTable() {
    }

    /**
     * 增加一行
     */
    public List<MarkdownComponent> addRow() {
        List<MarkdownComponent> list = new ArrayList<>();
        tables.add(list);
        return list;
    }

    public List<MarkdownComponent> addRow(int i) {
        List<MarkdownComponent> list = new ArrayList<>();
        tables.add(i, list);
        return list;
    }

    /**
     * 获取行
     */
    public List<MarkdownComponent> getRow(int i) {
        return tables.get(i);
    }

    /**
     * 删除行
     */
    public List<MarkdownComponent> removeRow(int i) {
        return tables.remove(i);
    }

    /**
     * 大小
     */
    public int rowLength() {
        return tables.size();
    }

    public int columnLength(int row) {
        return tables.get(row).size();
    }

    public int maxColumn() {
        int max = 0;
        for (List<MarkdownComponent> row : tables) {
            if (row.size() > max) {
                max = row.size();
            }
        }
        return max;
    }

    /**
     * 设置排列方式
     *
     * @param column 第几列
     * @param align  排列方式
     */
    public void setAlign(int column, int align) {
        aligns.put(column, align);
    }

    public void setDefaultAlign(int align) {
        defaultAlign = align;
    }

    public int getAlign(int column) {
        return aligns.get(column);
    }

    public int getDefaultAlign() {
        return defaultAlign;
    }

    @Override
    public Iterator<List<MarkdownComponent>> iterator() {
        return tables.iterator();
    }

    @Override
    public void forEach(Consumer<? super List<MarkdownComponent>> action) {
        tables.forEach(action);
    }

    @Override
    public Spliterator<List<MarkdownComponent>> spliterator() {
        return tables.spliterator();
    }

    @Override
    public String toString() {
        if (tables.isEmpty()) {
            return "";
        }
        int maxColumn = maxColumn();
        StringBuilder builder = new StringBuilder("|");
        Iterator<List<MarkdownComponent>> it = tables.iterator();
        //title
        List<MarkdownComponent> titles = it.next();
        for (int i = 0, titleSize = titles.size(); i < maxColumn; i++) {
            if (i < titleSize) {
                builder.append(" ").append(titles.get(i)).append(" |");
            } else {
                builder.append(" |");
            }
        }
        builder.append("\n|");
        //排列
        for (int i = 0; i < maxColumn; i++) {
            switch (aligns.getOrDefault(i, defaultAlign)) {
                case ALIGN_LEFT:
                    builder.append(":--------|");
                    break;
                case ALIGN_CENTER:
                    builder.append(":-------:|");
                    break;
                case ALIGN_RIGHT:
                    builder.append("--------:|");
                    break;
                default:
                    builder.append(":--------|");
                    break;
            }
        }
        while (it.hasNext()) {
            builder.append("\n|");
            List<MarkdownComponent> row = it.next();
            for (int i = 0, rowSize = row.size(); i < maxColumn; i++) {
                if (i < rowSize) {
                    builder.append(" ").append(row.get(i)).append(" |");
                } else {
                    builder.append(" |");
                }
            }
        }
        return builder.toString();
    }


    @Override
    public MarkdownTable clone() {
        try {
            MarkdownTable markdownTable = (MarkdownTable) super.clone();
            markdownTable.tables = new ArrayList<>(this.tables.size());
            for (List<MarkdownComponent> row : this.tables) {
                List<MarkdownComponent> list = new ArrayList<>(row.size());
                for (MarkdownComponent component : row) {
                    list.add(component.clone());
                }
                markdownTable.tables.add(list);
            }
            markdownTable.aligns = new HashMap<>(this.aligns);
            return markdownTable;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

}



