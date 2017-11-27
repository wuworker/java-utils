package com.wxl.utils.net.jdbc;

import com.wxl.utils.net.jdbc.impl.SnakeToCamelMapping;
import lombok.Data;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by wuxingle on 2017/11/24.
 * jdbc测试
 */
public class JdbcUtilTest {

    private static String url = "jdbc:mysql://127.0.0.1:3306/test_db?useSSL=false";

    private static String username = "root";

    private static String password = "123456";

    private static String driver = "com.mysql.jdbc.Driver";

    private static JdbcUtil jdbcUtil;

    @Data
    public static class User {
        private Integer id;

        private String name;

        private Integer age;

        private String createTime;

        private Double money;
    }

    @BeforeClass
    public static void before(){
        try {
            jdbcUtil = new JdbcUtil(driver,url,username,password);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }


    @Test
    public void testQuerySingleField()throws SQLException{
        List<Object> list = jdbcUtil.querySingleField("select name from t_user");
        System.out.println(list);
    }


    @Test
    public void testQueryMap()throws SQLException {
        List<Map<String, Object>> query = jdbcUtil.query("select id,name,age from t_user where id>? and name like ?",
                1,"%a%");
        System.out.println(query);
    }


    @Test
    public void testQueryClass()throws Exception {
        List<User> users = jdbcUtil.query(User.class,new SnakeToCamelMapping(),
                "select id,name,age,create_time,money from t_user where name like ?","%e");
        System.out.println(users);
    }

    @Test
    public void testUpdate()throws Exception {
        int count = jdbcUtil.update("insert into t_user(name,age) values(?,?)","哈哈1",24);
        System.out.println(count);
    }

    @Test
    public void testTransaction()throws Exception {
        jdbcUtil.startTransaction();
        try {
            jdbcUtil.update("insert into t_user(name,age) values(?,?)","哈哈2",24);
            jdbcUtil.update("insert into t_user(name,age) values(?,?)","哈哈2",24);
            jdbcUtil.commit();
        }catch (Exception e){
            e.printStackTrace();
            jdbcUtil.rollback();
        }
    }


    @AfterClass
    public static void end()throws SQLException {
        if(jdbcUtil!=null){
            jdbcUtil.close();
        }
    }






}