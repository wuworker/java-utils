package com.wxl.utils.file.md;

import lombok.Data;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by wuxingle on 2018/05/22
 * md列表
 */
@Data
public class MarkdownList implements MarkdownComponent, Iterable<MarkdownComponent> {

    private static final long serialVersionUID = 6231039598975292869L;

    //有序true
    private boolean order;

    private List<MarkdownComponent> list;

    public MarkdownList(List<MarkdownComponent> list) {
        this(list, true);
    }

    public MarkdownList(List<MarkdownComponent> list, boolean order) {
        Assert.notNull(list, "list can not null");
        this.order = order;
        this.list = list;
    }


    public void setList(List<MarkdownComponent> list) {
        Assert.notNull(list, "list can not null");
        this.list = list;
    }


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
        if (order) {
            for (int i = 0; i < list.size(); i++) {
                builder.append(i + 1).append(". ").append(list.get(i)).append("\n");
            }
        } else {
            for (MarkdownComponent component : list) {
                builder.append("- ").append(component).append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    public MarkdownList clone() {
        try {
            MarkdownList markdownList = (MarkdownList) super.clone();
            if (this.list != null) {
                markdownList.list = new ArrayList<>(this.list.size());
                for (MarkdownComponent component : this.list) {
                    markdownList.list.add(component.clone());
                }
            } else {
                markdownList.list = null;
            }
            return markdownList;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

}


