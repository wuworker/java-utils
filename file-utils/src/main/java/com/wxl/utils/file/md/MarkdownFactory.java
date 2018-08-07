package com.wxl.utils.file.md;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuxingle on 2018/05/23
 * 生成md组件工厂
 * 推荐使用工厂生成组件，这样可以返回子类
 */
public abstract class MarkdownFactory {

    /**
     * text
     */
    public MarkdownText getText(String text) {
        return getText(text, false, false);
    }

    public MarkdownText getItalicText(String text) {
        return getText(text, true, false);
    }

    public MarkdownText getBoldText(String text) {
        return getText(text, false, true);
    }

    public abstract MarkdownText getText(String text, boolean italic, boolean bold);

    /**
     * title
     */
    public MarkdownTitle getTitle(String text) {
        return getTitle(getText(text), MarkdownTitle.MIN_LEVEL);
    }

    public MarkdownTitle getTitle(String text, int level) {
        return getTitle(getText(text), level);
    }

    public MarkdownTitle getTitle(MarkdownComponent text) {
        return getTitle(text, MarkdownTitle.MIN_LEVEL);
    }

    public abstract MarkdownTitle getTitle(MarkdownComponent text, int level);

    /**
     * link
     */
    public MarkdownLink getLink(String desc, String url) {
        return getLink(getText(desc), url);
    }

    public abstract MarkdownLink getLink(MarkdownComponent desc, String url);

    /**
     * image
     */
    public MarkdownImage getImage(String desc, String url) {
        return getImage(getText(desc), url);
    }

    public abstract MarkdownImage getImage(MarkdownComponent desc, String url);


    /**
     * code
     */
    public MarkdownCode getCode(String code) {
        return getCode(code, null);
    }

    public abstract MarkdownCode getCode(String code, String lang);

    /**
     * laTex
     */
    public MarkdownLaTex getLatex(String text) {
        return getLatex(text, false);
    }

    public abstract MarkdownLaTex getLatex(String text, boolean inline);


    /**
     * quote
     */
    public MarkdownQuote getQuote(String content) {
        return getQuote(getText(content), 1);
    }

    public MarkdownQuote getQuote(String content, int level) {
        return getQuote(getText(content), level);
    }

    public MarkdownQuote getQuote(MarkdownComponent content) {
        return getQuote(content, 1);
    }

    public abstract MarkdownQuote getQuote(MarkdownComponent content, int level);

    /**
     * list
     */
    public MarkdownList getListFromString(String... list) {
        return getListFromString(true, list);
    }

    public MarkdownList getListFromString(boolean order, String... list) {
        List<MarkdownComponent> components = new ArrayList<>(list.length);
        for (String str : list) {
            components.add(getText(str));
        }
        return getList(components, order);
    }

    public MarkdownList getListFromString(List<String> list) {
        return getListFromString(true, list);
    }

    public MarkdownList getListFromString(boolean order, List<String> list) {
        List<MarkdownComponent> components = new ArrayList<>(list.size());
        for (String str : list) {
            components.add(getText(str));
        }
        return getList(components, order);
    }

    public MarkdownList getList(List<MarkdownComponent> list) {
        return getList(list, true);
    }

    public abstract MarkdownList getList(List<MarkdownComponent> list, boolean order);

    /**
     * table
     */
    public abstract MarkdownTable getTable();

}


