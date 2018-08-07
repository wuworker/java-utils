package com.wxl.utils.file.md;

import lombok.Data;

/**
 * Created by wuxingle on 2018/05/23
 * md的公式
 */
@Data
public class MarkdownLaTex implements MarkdownComponent {

    private static final long serialVersionUID = -6733261484643829167L;

    //true行内，false整行
    private boolean inline;

    private String text;

    public MarkdownLaTex(String text) {
        this(text, false);
    }

    public MarkdownLaTex(String text, boolean inline) {
        this.text = text;
        this.inline = inline;
    }

    @Override
    public String toString() {
        String ht = inline ? "$" : "$$";
        return ht + " " + text + " " + ht;
    }

    @Override
    public MarkdownLaTex clone() {
        try {
            return (MarkdownLaTex) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
