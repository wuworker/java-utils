package com.wxl.utils.file.md;

import org.junit.Test;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuxingle on 2018/05/22
 */
public class MarkdownComponentTest {

    @Test
    public void testComponent() {
        MarkdownText word = new MarkdownText("nicee");
        word.setBold(true);
        word.setItalic(true);

        System.out.println(word);

        MarkdownTitle title1 = new MarkdownTitle(new MarkdownText("标题1"));
        System.out.println(title1);

        MarkdownTitle title2 = new MarkdownTitle(new MarkdownText("标题2"));
        System.out.println(title2);

        MarkdownLink link = new MarkdownLink(new MarkdownText("百度", true), "http://www.baidu.com");
        System.out.println(link);

        MarkdownImage image = new MarkdownImage(new MarkdownText("nice"), "https://www.baidu.com/img/bd_logo1.png");
        System.out.println(image);

        List<MarkdownComponent> list = new ArrayList<>();
        list.add(new MarkdownText("gg"));
        list.add(new MarkdownText("aa", true));
        list.add(new MarkdownText("cc", false, true));
        MarkdownList markdownList = new MarkdownList(list, false);
        System.out.println(markdownList);

        MarkdownQuote quote = new MarkdownQuote(markdownList, 1);
        System.out.println(quote);

        MarkdownCode code = new MarkdownCode("sjfahfsf", "a");
        System.out.println(code);

        MarkdownTable table = new MarkdownTable();
        List<MarkdownComponent> list1 = table.addRow();
        list1.add(new MarkdownText("a1"));
        list1.add(new MarkdownText("b1"));
        list1.add(new MarkdownText("c1"));
        List<MarkdownComponent> list2 = table.addRow();
        list2.add(new MarkdownText("a2"));
        list2.add(new MarkdownText("b2"));
        list2.add(new MarkdownText("c2"));
        List<MarkdownComponent> list3 = table.addRow();
        list3.add(new MarkdownText("a3"));
        list3.add(new MarkdownText("b3"));
        list3.add(new MarkdownText("c3"));

        table.setAlign(1, MarkdownTable.ALIGN_CENTER);
        System.out.println(table);
    }

    @Test
    public void testClone() {
        MarkdownText t1 = new MarkdownText("gg");
        MarkdownText t2 = t1.clone();
        System.out.println(t1 == t2);
        System.out.println(t1);
        System.out.println(t2);
    }

    @Test
    public void testSerialize() throws IOException {
        MarkdownTitle t1 = new MarkdownTitle(new MarkdownText("gg"));
        byte[] bytes = SerializationUtils.serialize(t1);
        MarkdownTitle t2 = (MarkdownTitle) SerializationUtils.deserialize(bytes);
        System.out.println(t1 == t2);
        System.out.println(t1);
        System.out.println(t2);
    }

    @Test
    public void testBook(){
        MarkdownBook book = new MarkdownBook();
        book.addTitle("通讯录查询",1)
                .addText("+ api")
                .addCode("POST /user")
                .addText("+ 参数")
                .addCode("{\n" +
                        "\t\"age\":20,\n" +
                        "\t\"name\":\"gg\"\n" +
                        "}")
                .addText("+ 参数说明");
        MarkdownTable table1 = new MarkdownTable();
        List<MarkdownComponent> row1 = table1.addRow();
        row1.add(new MarkdownText("参数"));
        row1.add(new MarkdownText("是否必须"));
        row1.add(new MarkdownText("说明"));
        List<MarkdownComponent> row2 = table1.addRow();
        row2.add(new MarkdownText("age"));
        row2.add(new MarkdownText("否"));
        row2.add(new MarkdownText("年龄"));
        List<MarkdownComponent> row3 = table1.addRow();
        row3.add(new MarkdownText("name"));
        row3.add(new MarkdownText("是"));
        row3.add(new MarkdownText("名字"));
        book.addTable(table1)
                .addNewLine()
                .addText("+ 返回值")
                .addCode("{\n" +
                        "\t\"data\":[\n" +
                        "\t\t{\n" +
                        "\t\t\t\"id\":1,\n" +
                        "\t\t\t\"list\":[\n" +
                        "\t\t\t\t{\n" +
                        "\t\t\t\t\t\"name\":\"gg\",\n" +
                        "\t\t\t\t\t\"age\":20,\n" +
                        "\t\t\t\t\t\"address\":\"ggg\",\n" +
                        "\t\t\t\t\t\"phone\":\"123456789\",\n" +
                        "\t\t\t\t\t\"idnum\":\"33030303030030303030\",\n" +
                        "\t\t\t\t\t\"modifyAt\":\"2018-01-30 14:59:35\",\n" +
                        "\t\t\t\t\t\"createAt\":\"2017-09-14 13:14:50\"\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t]\n" +
                        "\t\t}\n" +
                        "\t],\n" +
                        "\t\"success\":true,\n" +
                        "\t\"resultCode\":0\n" +
                        "}")
                .addText("+ 返回说明");
        MarkdownTable table2 = new MarkdownTable();
        List<MarkdownComponent> row21 = table2.addRow();
        row21.add(new MarkdownText("参数"));
        row21.add(new MarkdownText("说明"));
        List<MarkdownComponent> row22 = table2.addRow();
        row22.add(new MarkdownText("data"));
        row22.add(new MarkdownText("数据"));
        List<MarkdownComponent> row23 = table2.addRow();
        row23.add(new MarkdownText("name"));
        row23.add(new MarkdownText("名字"));

        book.addTable(table2);
        System.out.println(book);
    }

}















