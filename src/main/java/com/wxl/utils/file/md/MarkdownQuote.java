package com.wxl.utils.file.md;

import lombok.Data;
import org.springframework.util.Assert;

/**
 * Created by wuxingle on 2018/05/22
 * md的引用
 */
@Data
public class MarkdownQuote implements MarkdownComponent {

    private static final long serialVersionUID = 6557968338910188919L;

    private MarkdownComponent content;

    //引用个数
    private int level;

    public MarkdownQuote(MarkdownComponent content) {
        this(content, 1);
    }

    public MarkdownQuote(MarkdownComponent content, int level) {
        Assert.isTrue(level > 0, "level must > 0");
        this.content = content;
        this.level = level;
    }

    public void setLevel(int level) {
        Assert.isTrue(level > 0, "level must > 0");
        this.level = level;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            builder.append(">");
        }
        return builder + " " + content;
    }

    @Override
    public MarkdownQuote clone() {
        try {
            MarkdownQuote markdownQuote = (MarkdownQuote) super.clone();
            markdownQuote.content = this.content == null ? null : this.content.clone();
            return markdownQuote;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
