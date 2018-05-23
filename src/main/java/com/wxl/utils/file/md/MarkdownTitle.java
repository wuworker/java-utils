package com.wxl.utils.file.md;

import lombok.Data;
import org.springframework.util.Assert;

/**
 * Created by wuxingle on 2018/05/22
 * md标题
 */
@Data
public class MarkdownTitle implements MarkdownComponent {

    private static final long serialVersionUID = -7663430755837857138L;

    public static final int MAX_LEVEL = 6;

    public static final int MIN_LEVEL = 1;

    //标题等级
    private int level;

    //标题
    private MarkdownComponent title;

    public MarkdownTitle(MarkdownComponent title) {
        this(title, MIN_LEVEL);
    }

    public MarkdownTitle(MarkdownComponent title, int level) {
        Assert.isTrue(level <= MAX_LEVEL && level >= MIN_LEVEL,
                "level must is (" + MIN_LEVEL + "~" + MAX_LEVEL + ")");
        this.title = title;
        this.level = level;
    }

    public void setLevel(int level) {
        Assert.isTrue(level <= MAX_LEVEL && level >= MIN_LEVEL,
                "level must is (" + MIN_LEVEL + "~" + MAX_LEVEL + ")");
        this.level = level;
    }

    @Override
    public String toString() {
        StringBuilder levelStr = new StringBuilder(6);
        for (int i = 0; i < level; i++) {
            levelStr.append("#");
        }
        return levelStr + " " + title;
    }

    @Override
    public MarkdownTitle clone() {
        try {
            MarkdownTitle markdownTitle = (MarkdownTitle) super.clone();
            markdownTitle.title = this.title == null ? null : this.title.clone();
            return markdownTitle;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
