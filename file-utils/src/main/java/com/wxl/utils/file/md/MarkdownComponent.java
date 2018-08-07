package com.wxl.utils.file.md;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

/**
 * Created by wuxingle on 2018/05/22
 * md组件接口
 */
public interface MarkdownComponent extends Cloneable, Serializable {

    /**
     * 克隆方法
     */
    MarkdownComponent clone();

    /**
     * 输出md元素
     */
    String toString();


    /**
     * 输出内容
     */
    default void write(Writer writer) throws IOException {
        writer.write(toString());
    }

    /**
     * 输出换行
     */
    default void writeln(Writer writer) throws IOException {
        write(writer);
        writer.write("\n");
    }

}

