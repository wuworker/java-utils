package com.wxl.utils.net.jdbc;

import com.wxl.utils.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

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
     */
    public void createTablesLike(String templateTableName, Collection<String> tableNames) throws SQLException {
        Set<String> sucTab = new HashSet<>();
        try {
            for (String tableName : tableNames) {
                jdbcUtils.update("create table " + tableName + " like " + templateTableName);
                sucTab.add(tableName);
                log.debug("create table {}", tableName);
            }
            log.debug("create table success,count is {}", tableNames.size());
        } catch (RuntimeException e) {
            //有异常,删除之前创建的表
            for (String tableName : sucTab) {
                jdbcUtils.update("drop table " + tableName);
            }
            throw e;
        } finally {
            jdbcUtils.close();
        }
    }


    /**
     * 删除表
     */
    public void dropTables(Collection<String> tableNames) throws SQLException {
        try {
            for (String name : tableNames) {
                jdbcUtils.update("drop table " + name);
                log.debug("drop table {} success", name);
            }
        } finally {
            jdbcUtils.close();
        }
    }

    /**
     * 清空所有数据
     */
    @IsTransaction
    public Map<String, Integer> clearTables(Collection<String> tableNames) throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>(tableNames.size());
        try {
            jdbcUtils.startTransaction();
            for (String table : tableNames) {
                int count = jdbcUtils.update("delete from " + table);
                result.put(table, count);
            }
            jdbcUtils.commit();
            log.info("clear table success:{}", result);
        } catch (RuntimeException e) {
            jdbcUtils.rollback();
            throw e;
        } finally {
            jdbcUtils.close();
        }
        return result;
    }

    /**
     * 对所有分表进行统计
     */
    public long countShardTables(String baseTable, Collection<String> tableNames, String sql, Object... params) throws SQLException {
        Map<String, Long> map = querySingle(Long.class, baseTable, tableNames, sql, params);
        long sum = 0;
        for (Long i : map.values()) {
            sum += i;
        }
        return sum;
    }

    /**
     * 对所有分表进行更新
     */
    @IsTransaction
    public Map<String, Integer> update(String baseTable, Collection<String> tableNames, String sql, Object... params) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        try {
            jdbcUtils.startTransaction();
            for (String table : tableNames) {
                String exeSql = sql.replaceAll(baseTable, table);
                int count = jdbcUtils.update(exeSql, params);
                map.put(table, count);
            }
            jdbcUtils.commit();
            return map;
        } catch (RuntimeException e) {
            jdbcUtils.rollback();
            throw e;
        } finally {
            jdbcUtils.close();
        }
    }

    /**
     * 从所有分表中查单行单列
     */
    public Map<String, Object> querySingle(String baseTable, Collection<String> tableNames,
                                           String sql, Object... params) throws SQLException {
        return querySingle(Object.class, baseTable, tableNames, sql, params);
    }

    public <T> Map<String, T> querySingle(final Class<T> clazz, String baseTable, Collection<String> tableNames,
                                          String sql, Object... params) throws SQLException {
        return query(list -> {
            if (list.size() > 1) {
                throw new SQLException("sql result row find " + list.size() + ",but expect is one!");
            }
            if (list.isEmpty()) {
                return null;
            }
            return ReflectUtils.castSafeOfNumber(clazz, list.get(0).values().iterator().next());
        }, baseTable, tableNames, sql, params);
    }

    /**
     * 从所有分表中查单行多列
     */
    public Map<String, Map<String, Object>> querySingleRow(String baseTable, Collection<String> tableNames,
                                                           String sql, Object... params) throws SQLException {
        return query(list -> {
            if (list.size() > 1) {
                throw new SQLException("sql result row find " + list.size() + ",but expect is one!");
            }
            if (list.isEmpty()) {
                return new HashMap<>();
            }
            return list.get(0);
        }, baseTable, tableNames, sql, params);
    }

    /**
     * 从所有分表中查多行单列
     */
    public Map<String, List<Object>> querySingleField(String baseTable, Collection<String> tableNames,
                                                      String sql, Object... params) throws SQLException {
        return querySingleField(Object.class, baseTable, tableNames, sql, params);
    }

    public <T> Map<String, List<T>> querySingleField(final Class<T> clazz, String baseTable, Collection<String> tableNames,
                                                     String sql, Object... params) throws SQLException {
        return query((list -> {
            List<T> result = new ArrayList<>();
            for (Map<String, Object> row : list) {
                result.add(ReflectUtils.castSafeOfNumber(clazz, row.values().iterator().next()));
            }
            return result;
        }), baseTable, tableNames, sql, params);
    }

    /**
     * 从所有分表中查多行多列
     */
    public Map<String, List<Map<String, Object>>> query(String baseTable, Collection<String> tableNames,
                                                        String sql, Object... params) throws SQLException {
        return query((list) -> list, baseTable, tableNames, sql, params);
    }


    /**
     * 表数据处理
     *
     * @param <T>
     */
    public interface TableHandler<T> {

        T handleTable(List<Map<String, Object>> list) throws SQLException;
    }

    /**
     * 从所有分表中查询
     */
    public <T> Map<String, T> query(TableHandler<T> handler, String baseTable, Collection<String> tableNames,
                                    String sql, Object... params) throws SQLException {
        Assert.notNull(handler, "table handler can not null");
        Map<String, T> map = new LinkedHashMap<>();
        try {
            for (String table : tableNames) {
                String exeSql = sql.replaceAll(baseTable, table);
                List<Map<String, Object>> result = jdbcUtils.query(exeSql, params);
                map.put(table, handler.handleTable(result));
            }
            return map;
        } finally {
            jdbcUtils.close();
        }
    }


    /**
     * 分表策略接口
     */
    public interface ShardStrategy {
        /**
         * 原始表的行数据
         */
        String getTableName(Map<String, Object> rowData);
    }

    /**
     * 把原来表中的数据分到分表中去
     */
    @IsTransaction
    @SuppressWarnings("unchecked")
    public void shardTableData(String tableName, int onceCount, ShardStrategy shardStrategy) throws SQLException {
        try {
            jdbcUtils.startTransaction();
            long all = jdbcUtils.querySingle(Long.class, "select count(*) from " + tableName);
            log.info("shard table all count:{}", all);
            int insertAllCount = 0;
            int start = 0;
            do {
                List<Map<String, Object>> rows = jdbcUtils.query("select * from " + tableName + " limit ?,?",
                        start, onceCount);
                log.debug("shard table start, {} to {}", start, start + onceCount);
                //数据分类
                //分表数据,key表名,value表数据
                Map<String, List<Map<String, Object>>> shardDatas = new HashMap<>();
                for (Map<String, Object> row : rows) {
                    String table = shardStrategy.getTableName(row);
                    List<Map<String, Object>> shardRow = shardDatas.computeIfAbsent(table, (k) -> new ArrayList<>());
                    shardRow.add(row);
                }
                //生成sql
                StringBuilder[] sqls = new StringBuilder[shardDatas.size()];
                String[] tableNames = new String[shardDatas.size()];
                List[] params = new ArrayList[shardDatas.size()];
                int i = 0;
                for (Map.Entry<String, List<Map<String, Object>>> shardData : shardDatas.entrySet()) {
                    String table = shardData.getKey();
                    List<Map<String, Object>> rs = shardData.getValue();
                    sqls[i] = new StringBuilder("insert into " + table + " values ");
                    params[i] = new ArrayList();
                    tableNames[i] = table;
                    for (Map<String, Object> row : rs) {
                        sqls[i].append("(");
                        for (Map.Entry<String, Object> column : row.entrySet()) {
                            params[i].add(column.getValue());
                            sqls[i].append("?,");
                        }
                        sqls[i] = sqls[i].replace(sqls[i].length() - 1, sqls[i].length(), "),");
                    }
                    sqls[i] = sqls[i].delete(sqls[i].length() - 1, sqls[i].length());
                    i++;
                }
                //执行sql
                for (i = 0; i < shardDatas.size(); i++) {
                    int count = jdbcUtils.update(sqls[i].toString(), params[i].toArray(new Object[params[i].size()]));
                    log.debug("shard table result,tableName:{},count:{}", tableNames[i], count);
                    insertAllCount += count;
                }
                log.debug("shard table end, {} to {}", start, start + onceCount);

                start += onceCount;
            } while (start < all);

            jdbcUtils.commit();
            log.info("shard table data end,all insert count:{}", insertAllCount);
        } catch (RuntimeException e) {
            jdbcUtils.rollback();
            throw e;
        } finally {
            jdbcUtils.close();
        }
    }


}





