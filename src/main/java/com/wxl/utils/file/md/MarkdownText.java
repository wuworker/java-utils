package com.wxl.utils.file.md;

import lombok.Data;

/**
 * Created by wuxingle on 2018/05/22
 * md的文字
 */
@Data
public class MarkdownText implements MarkdownComponent {

    private static final long serialVersionUID = 8438936973657575585L;

    //斜体
    private boolean italic;

    //粗体
    private boolean bold;

    //内容
    private String text;

    public MarkdownText(String text) {
        this.text = text;
    }

    public MarkdownText(String text, boolean italic) {
        this.text = text;
        this.italic = italic;
    }

    public MarkdownText(String text, boolean italic, boolean bold) {
        this.text = text;
        this.italic = italic;
        this.bold = bold;
    }

    @Override
    public String toString() {
        StringBuilder decorate = new StringBuilder(3);
        if (italic)
            decorate.append("*");
        if (bold)
            decorate.append("**");
        String decoStr = decorate.toString();

        return decoStr + text + decoStr;
    }

    @Override
    public MarkdownText clone() {
        try {
            return (MarkdownText) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

}

