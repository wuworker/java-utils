package com.wxl.utils.file.md;

import lombok.Data;

/**
 * Created by wuxingle on 2018/05/22
 * md链接
 */
@Data
public class MarkdownLink implements MarkdownComponent {

    private static final long serialVersionUID = -2412366644264242309L;

    //描述
    private MarkdownComponent desc;

    //url
    private String url;

    public MarkdownLink(MarkdownComponent desc, String url) {
        this.desc = desc;
        this.url = url;
    }

    @Override
    public String toString() {
        return "[" + desc + "](" + url + ")";
    }

    @Override
    public MarkdownLink clone() {
        try {
            MarkdownLink markdownList = (MarkdownLink) super.clone();
            markdownList.desc = this.desc == null ? null : this.desc.clone();
            return markdownList;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

}
