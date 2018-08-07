package com.wxl.utils.jdbc;

import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        shardTableHelper.createTablesLike(templateTable, shardTables);
    }


    @Test
    public void testDropShardTable() throws SQLException {
        shardTableHelper.dropTables(shardTables);
    }

    @Test
    public void testClearTables() throws SQLException {
        Map<String, Integer> map = shardTableHelper.clearTables(shardTables);
        System.out.println(map);
    }

    @Test
    public void testQuery()throws SQLException{
        Map<String, Integer> map = shardTableHelper.querySingle(Integer.class, templateTable, shardTables,
                "select count(*) from user_info");
        System.out.println(map);
        Map<String, List<String>> singleField = shardTableHelper.querySingleField(String.class, templateTable, shardTables,
                "select name from user_info");
        System.out.println(singleField);
        Map<String, Map<String, Object>> singleRow = shardTableHelper.querySingleRow(templateTable, shardTables,
                "select * from user_info limit 1");
        System.out.println(singleRow);

        Map<String, List<Map<String, Object>>> all = shardTableHelper.query(templateTable, shardTables,
                "select name,age from user_info where createTime > ?","2010-01-01 00:00:00");
        System.out.println(all);
    }

    @Test
    public void testCount()throws SQLException{
        long sumAge1 = shardTableHelper.countShardTables(templateTable,shardTables,
                "select sum(age) from user_info");
        long allCount = shardTableHelper.countShardTables(templateTable,shardTables,
                "select count(*) from user_info");
        System.out.println(sumAge1);
        System.out.println(allCount);
    }

    @Test
    public void testShardTableData() throws SQLException {
        shardTableHelper.shardTableData("user_info", 10, (data) -> {
            int id = (int) data.get("id");
            int f = id % 5 + 1;
            return "user_info_" + f;
        });
    }

}






