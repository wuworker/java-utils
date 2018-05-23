package com.wxl.utils.file.md;

import java.util.List;

/**
 * Created by wuxingle on 2018/05/23
 * md组件工厂默认实现
 * 简单对象的new比clone快
 */
public class DefaultMarkdownFactory extends MarkdownFactory {

    @Override
    public MarkdownText getText(String text, boolean italic, boolean bold) {
        return new MarkdownText(text, italic, bold);
    }

    @Override
    public MarkdownTitle getTitle(MarkdownComponent text, int level) {
        return new MarkdownTitle(text, level);
    }

    @Override
    public MarkdownLink getLink(MarkdownComponent desc, String url) {
        return new MarkdownLink(desc, url);
    }

    @Override
    public MarkdownImage getImage(MarkdownComponent desc, String url) {
        return new MarkdownImage(desc, url);
    }

    @Override
    public MarkdownCode getCode(String code, String lang) {
        return new MarkdownCode(code, lang);
    }

    @Override
    public MarkdownLaTex getLatex(String text, boolean inline) {
        return new MarkdownLaTex(text, inline);
    }

    @Override
    public MarkdownQuote getQuote(MarkdownComponent content, int level) {
        return new MarkdownQuote(content, level);
    }

    @Override
    public MarkdownList getList(List<MarkdownComponent> list, boolean order) {
        return new MarkdownList(list, order);
    }

    @Override
    public MarkdownTable getTable() {
        return new MarkdownTable();
    }

}
