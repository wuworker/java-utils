package com.wxl.utils.net.jdbc;

import com.wxl.utils.net.jdbc.impl.SnakeToCamelMapping;
import lombok.Data;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wuxingle on 2017/11/24.
 * jdbc测试
 */
public class JdbcUtilsTest {

    private static String url = "jdbc:mysql://127.0.0.1:3306/wxl_test?useSSL=false";

    private static String username = "root";

    private static String password = "123456";

    private static String driver = "com.mysql.jdbc.Driver";

    private static JdbcUtils jdbcUtils;

    @Data
    public static class User {
        private Integer id;

        private String name;

        private Integer age;

        private String createTime;

        private Double money;
    }

    @BeforeClass
    public static void before() {
        try {
            jdbcUtils = new JdbcUtils(driver, url, username, password);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testCreateTab()throws SQLException{
        Object show_create_table_user_info = jdbcUtils.query("show create table user_info");
        System.out.println(show_create_table_user_info);
    }


    @Test
    public void testQuerySingleField() throws SQLException {
        List<Object> list = jdbcUtils.querySingleField("select name from t_user");
        System.out.println(list);
    }


    @Test
    public void testQueryMap() throws SQLException {
        List<Map<String, Object>> query = jdbcUtils.query("select id,name,age from t_user where id>? and name like ?",
                1, "%a%");
        System.out.println(query);
    }


    @Test
    public void testQueryClass() throws Exception {
        List<User> users = jdbcUtils.query(User.class, new SnakeToCamelMapping(),
                "select id,name,age,create_time,money from t_user where name like ?", "%e");
        System.out.println(users);
    }

    @Test
    public void testUpdate() throws Exception {
        int count = jdbcUtils.update("insert into t_user(name,age) values(?,?)", "哈哈1", 24);
        System.out.println(count);
    }

    @Test
    public void testTransaction() throws Exception {
        jdbcUtils.startTransaction();
        try {
            jdbcUtils.update("insert into t_user(name,age) values(?,?)", "哈哈2", 24);
            jdbcUtils.update("insert into t_user(name,age) values(?,?)", "哈哈2", 24);
            jdbcUtils.commit();
        } catch (Exception e) {
            e.printStackTrace();
            jdbcUtils.rollback();
        }
    }


    @Test
    public void test() {
        int threadNum = 100;
        ExecutorService service = Executors.newFixedThreadPool(threadNum);
        CountDownLatch downLatch1 = new CountDownLatch(threadNum);
        CountDownLatch downLatch2 = new CountDownLatch(threadNum);
        for (int i = 0; i < threadNum; i++) {
            service.submit(() -> {
                try {
                    downLatch1.countDown();
                    downLatch1.await();
                    String threadName = Thread.currentThread().getName();
                    jdbcUtils.startTransaction();
                    List<Object> list = jdbcUtils.querySingleField("select money from t_user where id=1");
                    System.out.println(threadName + ":" + list);
                    jdbcUtils.update("update t_user set money=money+1 where id=1");
                    jdbcUtils.commit();
                    jdbcUtils.close();
                    System.out.println(threadName + ",end");
                    downLatch2.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        try {
            downLatch2.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.shutdown();
    }


    @AfterClass
    public static void end() throws SQLException {
        if (jdbcUtils != null) {
            jdbcUtils.close();
        }
    }


}