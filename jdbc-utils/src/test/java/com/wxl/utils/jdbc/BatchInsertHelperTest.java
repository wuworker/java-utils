package com.wxl.utils.jdbc;

import lombok.Data;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Date;
import java.util.Random;

/**
 * Created by wuxingle on 2017/12/12.
 *
 */
public class BatchInsertHelperTest {


    private static String url = "jdbc:mysql://127.0.0.1:3306/wxl_test?useSSL=false";

    private static String username = "root";

    private static String password = "123456";

    private static String driver = "com.mysql.jdbc.Driver";

    private static BatchInsertHelper insertHelper;

    @BeforeClass
    public static void before(){
        try {
            insertHelper = new BatchInsertHelper(driver,url,username,password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    static class User {
        private Long id;
        private String name;
        private Integer age;
        private String password;
        private Date createTime;

        public User(){}
    }

    @Test
    public void testInvokeBatchInsert() throws Exception {
        Random random = new Random();
        EntityGenerator<User> entityGenerator = new EntityGenerator<>(random,User.class);
        int count = insertHelper.invokeBatchInsert("user_info", User.class, 200_001,100_000, entityGenerator);

        System.out.println(count);
    }


    @AfterClass
    public static void after(){
        try {
            insertHelper.shutdown();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}