package com.wxl.utils.file.md;

import lombok.Data;

/**
 * Created by wuxingle on 2018/05/22
 * md图片
 */
@Data
public class MarkdownImage implements MarkdownComponent {

    private static final long serialVersionUID = -7961033939332221000L;

    private MarkdownComponent desc;

    private String url;

    private boolean showDesc;

    public MarkdownImage(MarkdownComponent desc, String url) {
        this(desc, url, false);
    }

    public MarkdownImage(MarkdownComponent desc, String url, boolean showDesc) {
        this.desc = desc;
        this.url = url;
        this.showDesc = showDesc;
    }

    @Override
    public String toString() {
        return (showDesc ? "![@" : "![") + desc + "](" + url + ")";
    }

    @Override
    public MarkdownImage clone() {
        try {
            MarkdownImage markdownImage = (MarkdownImage) super.clone();
            markdownImage.desc = this.desc == null ? null : this.desc.clone();
            return markdownImage;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}

