package com.wxl.utils.net.jdbc;

import lombok.Data;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

/**
 * Created by wuxingle on 2017/12/12.
 */
public class EntityGeneratorTest {

    @Data
    private static class App {
        private Integer id;
        private String name;
        private int age;
        private String address;
        private Date createTime;
        private Long haha;
        private Byte lala;
        private BigDecimal gg;
        private boolean nice;
        private float aa;
        private double bb;
        private Object obj;
    }



    @Test
    public void testGenerate() {
        Random random = new Random();
        EntityGenerator<App> entityGenerator = new EntityGenerator<>(random, App.class);
        App app1 = entityGenerator.generateEntity(1);
        App app2 = entityGenerator.generateEntity(2);

        System.out.println(app1);
        System.out.println(app2);
    }


}