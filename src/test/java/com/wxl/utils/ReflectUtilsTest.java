package com.wxl.utils;

import org.junit.Test;

import java.lang.reflect.Field;

/**
 * Created by wuxingle on 2017/11/24 0024.
 * 反射测试
 */
public class ReflectUtilsTest {

    public class A {

        private String name = "哈哈";

        public String getName() {
            return "["+name+"]";
        }

        public void setName(String name){
            this.name = "{"+name+"}";
        }
    }

    public class B extends A {
        private String age = "10";

        private boolean man = true;

        public boolean isMan() {
            System.out.println("被调用");
            return man;
        }

        public void setAge(String age) {
            System.out.println("setage 调用");
            this.age = age;
        }
    }

    @Test
    public void testGetFieldValue()throws Exception {
        B b = new B();
        Field nameField = B.class.getSuperclass().getDeclaredField("name");
        Object value = ReflectUtils.getObjectValue(b,nameField);
        System.out.println(value);
        Object value2 = ReflectUtils.getObjectValue(b,B.class.getDeclaredField("age"));
        System.out.println(value2);
        Object value3 = ReflectUtils.getObjectValue(b,B.class.getDeclaredField("man"));
        System.out.println(value3);
    }


    @Test
    public void testSetFieldValue()throws Exception {
        B b = new B();
        Field nameField = B.class.getSuperclass().getDeclaredField("name");
        ReflectUtils.setObjectValue(b,nameField,"拉拉");
        Object value = ReflectUtils.getObjectValue(b,nameField);
        System.out.println(value);

        Field ageField = B.class.getDeclaredField("age");
        ReflectUtils.setObjectValue(b,ageField,"100");
        Object value2 = ReflectUtils.getObjectValue(b,ageField);
        System.out.println(value2);
    }













}