package com.wxl.utils.file.md;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by wuxingle on 2018/05/22
 * markdown文件
 */
public class MarkdownBook implements MarkdownComponent, Iterable<MarkdownComponent> {

    private static final long serialVersionUID = -2841083486426131158L;

    private List<MarkdownComponent> list = new ArrayList<>();

    private MarkdownFactory factory;

    public MarkdownBook() {
        this(new DefaultMarkdownFactory());
    }

    public MarkdownBook(MarkdownFactory factory) {
        this.factory = factory;
    }

    /**
     * newline
     */
    public MarkdownBook addNewLine() {
        return addText("\n");
    }

    /**
     * text
     */
    public MarkdownBook addText(String text) {
        list.add(factory.getText(text));
        return this;
    }

    public MarkdownBook addItalicText(String text) {
        list.add(factory.getItalicText(text));
        return this;
    }

    public MarkdownBook addBoldText(String text) {
        list.add(factory.getBoldText(text));
        return this;
    }

    public MarkdownBook addText(String text, boolean italic, boolean bold) {
        list.add(factory.getText(text, italic, bold));
        return this;
    }

    /**
     * title
     */
    public MarkdownBook addTitle(String text) {
        list.add(factory.getTitle(text));
        return this;
    }

    public MarkdownBook addTitle(String text, int level) {
        list.add(factory.getTitle(text, level));
        return this;
    }

    /**
     * link
     */
    public MarkdownBook addLink(String desc, String url) {
        list.add(factory.getLink(desc, url));
        return this;
    }

    /**
     * image
     */
    public MarkdownBook addImage(String desc, String url) {
        list.add(factory.getImage(desc, url));
        return this;
    }

    /**
     * code
     */
    public MarkdownBook addCode(String code) {
        list.add(factory.getCode(code));
        return this;
    }

    public MarkdownBook addCode(String code, String lang) {
        list.add(factory.getCode(code, lang));
        return this;
    }

    /**
     * latex
     */
    public MarkdownBook addLatex(String text) {
        list.add(factory.getLatex(text));
        return this;
    }

    public MarkdownBook addLatex(String text, boolean inline) {
        list.add(factory.getLatex(text, inline));
        return this;
    }

    /**
     * quote
     */
    public MarkdownBook addQuote(String content) {
        list.add(factory.getQuote(content));
        return this;
    }

    public MarkdownBook addQuote(String content, int index) {
        list.add(factory.getQuote(content, index));
        return this;
    }

    /**
     * list
     */
    public MarkdownBook addList(String... list) {
        this.list.add(factory.getListFromString(list));
        return this;
    }

    public MarkdownBook addList(boolean order, String... list) {
        this.list.add(factory.getListFromString(order, list));
        return this;
    }

    public MarkdownBook addList(List<String> list) {
        this.list.add(factory.getListFromString(list));
        return this;
    }

    public MarkdownBook addList(boolean order, List<String> list) {
        this.list.add(factory.getListFromString(order, list));
        return this;
    }

    /**
     * table
     */
    public MarkdownBook addTable(MarkdownTable table) {
        return add(table);
    }

    /**
     * custom
     */
    public MarkdownBook add(MarkdownComponent component) {
        list.add(component);
        return this;
    }

    public MarkdownBook add(int i, MarkdownComponent component) {
        list.add(i, component);
        return this;
    }

    /**
     * 大小
     */
    public int size() {
        return list.size();
    }

    /**
     * get
     */
    public MarkdownComponent get(int i) {
        return list.get(i);
    }

    @SuppressWarnings("unchecked")
    public <T extends MarkdownComponent> T get(int i, Class<T> clazz) {
        return (T) list.get(i);
    }


    /**
     * iterator
     */
    @Override
    public void forEach(Consumer<? super MarkdownComponent> action) {
        list.forEach(action);
    }

    @Override
    public Spliterator<MarkdownComponent> spliterator() {
        return list.spliterator();
    }

    @Override
    public Iterator<MarkdownComponent> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        if (list.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (MarkdownComponent component : list) {
            builder.append(component == null ? "\n" : component).append("\n");
        }
        return builder.toString();
    }

    @Override
    public MarkdownBook clone() {
        try {
            MarkdownBook book = (MarkdownBook) super.clone();
            book.list = new ArrayList<>(this.list.size());
            for (MarkdownComponent component : this.list) {
                if (component != null) {
                    book.list.add(component.clone());
                }
            }
            return book;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

}
