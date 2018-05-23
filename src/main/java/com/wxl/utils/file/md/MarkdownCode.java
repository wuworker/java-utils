package com.wxl.utils.file.md;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * Created by wuxingle on 2018/05/22
 * md的代码块
 */
@Data
public class MarkdownCode implements MarkdownComponent {

    private static final long serialVersionUID = 2921881337952030997L;

    private String lang;

    private String text;

    public MarkdownCode(String text) {
        this.text = text;
    }

    public MarkdownCode(String text, String lang) {
        this.text = text;
        this.lang = lang;
    }

    @Override
    public String toString() {
        return "```" + (StringUtils.hasText(lang) ? lang : "") + "\n"
                + text
                + "\n```";
    }

    @Override
    public MarkdownCode clone() {
        try {
            return (MarkdownCode) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
