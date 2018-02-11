package com.wxl.utils.net.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by wuxingle on 2018/02/08
 * 分表创建、查询类
 */
@Slf4j
public class ShardTableHelper {


    private JdbcUtils jdbcUtils;

    public ShardTableHelper(String driver, String url, String username, String password) throws ClassNotFoundException {
        jdbcUtils = new JdbcUtils(driver, url, username, password, false);
    }


    /**
     * 以模版表为基础创建分表
     *
     * @param templateTableName 基础表
     * @param tableNames        分表表名
     * @param rollback          失败是否回滚
     */
    public void createShardTable(String templateTableName, Collection<String> tableNames, boolean rollback) throws SQLException {
        Set<String> sucTab = new HashSet<>();
        try {
            for (String tableName : tableNames) {
                jdbcUtils.update("create table " + tableName + " like " + templateTableName);
                sucTab.add(tableName);
                log.debug("create table {}", tableName);
            }
            log.debug("create table success,count is {}", tableNames.size());
        } catch (SQLException e) {
            if (rollback) {
                for (String tableName : sucTab) {
                    jdbcUtils.update("drop table " + tableName);
                }
            }
            throw e;
        } finally {
            jdbcUtils.close();
        }
    }

    public void createShardTable(String templateTableName, Collection<String> tableNames) throws SQLException {
        createShardTable(templateTableName, tableNames, true);
    }


    /**
     * 删除分表,非事务
     */
    public void dropShardTable(Collection<String> tableNames) throws SQLException {
        try {
            for (String name : tableNames) {
                jdbcUtils.update("drop table " + name);
                log.debug("drop table {} success", name);
            }
        } finally {
            jdbcUtils.close();
        }
    }


    public int clearOfShardTable(String templateTableName,Collection<String> tableNames, String sql) throws SQLException {
        return 0;
    }


    /**
     * 对每个分表执行查询,使用replaceAll进行表名替换
     * @param templateTableName 基础表名
     * @param tableNames 分表
     * @param sql 执行的查询sql,里面用基础表名
     * @param params 参数
     */
    public Map<String, List<Map<String, Object>>> queryOfShardTable(String templateTableName,Collection<String> tableNames, String sql, Object... params) throws SQLException {
        Map<String,List<Map<String,Object>>> map = new LinkedHashMap<>();
        try {
            for(String tableName : tableNames){
                String exeSql = sql.replaceAll(templateTableName,tableName);
                List<Map<String, Object>> query = jdbcUtils.query(exeSql, params);
                map.put(tableName,query);
            }
        }finally {
            jdbcUtils.close();
        }
        return map;
    }

    public Map<String, Object> querySingleOfShardTable(String templateTableName,Collection<String> tableNames, String sql, Object... params) throws SQLException {
        Map<String,Object> map = new LinkedHashMap<>();
        try {
            for(String tableName : tableNames){
                String exeSql = sql.replaceAll(templateTableName,tableName);
                Object query = jdbcUtils.querySingleRowAndField(exeSql, params);
                map.put(tableName,query);
            }
        }finally {
            jdbcUtils.close();
        }
        return map;
    }

    /**
     * 对查询格式化成字符串
     */
    public String formatQueryOfShardTable(String templateTableName, Collection<String> tableNames, String sql, Object... params) throws SQLException {
        Map<String, List<Map<String, Object>>> map = queryOfShardTable(templateTableName,tableNames,sql,params);
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String,List<Map<String, Object>>> entry : map.entrySet()){
            sb.append(entry.getKey()).append(":\n");
            List<Map<String, Object>> rows = entry.getValue();
            for(Map<String,Object> row : rows){
                sb.append(row).append("\n");
            }
        }
        return sb.toString();
    }


    public <T> T executeOfShardTable(Collection<String> tableNames, String sql, Object... params) throws SQLException {
        return null;
    }

}





