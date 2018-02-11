package com.wxl.utils.net.jdbc;

import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuxingle on 2018/02/08
 */
public class ShardTableHelperTest {


    private static String url = "jdbc:mysql://127.0.0.1:3306/wxl_test?useSSL=false";

    private static String username = "root";

    private static String password = "123456";

    private static String driver = "com.mysql.jdbc.Driver";

    private static ShardTableHelper shardTableHelper;

    private static String templateTable = "user_info";

    private static List<String> shardTables = new ArrayList<>();

    @BeforeClass
    public static void before() {
        try {
            shardTableHelper = new ShardTableHelper(driver, url, username, password);
            for (int i = 1; i <= 5; i++) {
                shardTables.add("user_info_" + i);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateShardTable() throws SQLException {
        shardTableHelper.createShardTable(templateTable, shardTables);
    }


    @Test
    public void testDropShardTable() throws SQLException {
        shardTableHelper.dropShardTable(shardTables);
    }


    @Test
    public void testQueryAll()throws SQLException {
        String data = shardTableHelper.formatQueryOfShardTable(templateTable, shardTables, "select * from user_info");
        System.out.println(data);
    }



}






