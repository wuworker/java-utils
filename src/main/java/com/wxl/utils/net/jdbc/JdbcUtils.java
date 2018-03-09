package com.wxl.utils.net.jdbc;

import com.wxl.utils.ReflectUtils;
import com.wxl.utils.annotation.ThreadSafe;
import lombok.Getter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by wuxingle on 2017/7/24.
 * jdbc工具类
 */
@ThreadSafe
public class JdbcUtils {

    @Getter
    private String driver;

    @Getter
    private String url;

    @Getter
    private String username;

    @Getter
    private String password;

    //是否自动关闭连接
    @Getter
    private boolean autoClose;

    //数据库连接
    private ThreadLocal<Connection> connections = new ThreadLocal<>();

    //当前是否在事务中
    private ThreadLocal<Boolean> transactionState = new ThreadLocal<>();


    public JdbcUtils(String driver, String url, String username, String password) throws ClassNotFoundException {
        this(driver, url, username, password, true);
    }


    public JdbcUtils(String driver, String url, String username, String password, boolean autoClose) throws ClassNotFoundException {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.autoClose = autoClose;
        Class.forName(driver);
    }

    /**
     * 更新
     */
    public int update(String sql, Object... params) throws SQLException {
        Connection connection = getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (!ObjectUtils.isEmpty(params)) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
            }
            return statement.executeUpdate();
        } finally {
            closeAuto(null);
        }
    }

    /**
     * 查一行单字段
     */
    public Object querySingle(String sql, Object... params) throws SQLException {
        return querySingle(Object.class, sql, params);
    }

    public <T> T querySingle(Class<T> clazz, String sql, Object... params) throws SQLException {
        Assert.notNull(clazz, "class can not null");
        List<Object> list = querySingleField(sql, params);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new SQLException("sql result find " + list.size() + ",but expect is one!");
        }
        Object o = list.get(0);
        if (o == null) return null;
        return ReflectUtils.castSafeOfNumber(clazz, o);
    }

    /**
     * 查一行多个字段
     */
    public Map<String, Object> querySingleRow(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> list = query(sql, params);
        if (list.size() > 1) {
            throw new SQLException("sql result row find " + list.size() + ",but expect is one!");
        }
        if (list.isEmpty()) {
            return new HashMap<>();
        }
        return list.get(0);
    }

    /**
     * 查询多行单个字段
     */
    public List<Object> querySingleField(String sql, Object... params) throws SQLException {
        return querySingleField(Object.class, sql, params);
    }

    public <T> List<T> querySingleField(final Class<T> clazz, String sql, Object... params) throws SQLException {
        return query((map) -> {
            if (map.size() > 1) {
                throw new SQLException("sql result field find " + map.size() + ", " + map + " ,but expect is one!");
            }
            if (map.isEmpty()) {
                return null;
            }
            return ReflectUtils.castSafeOfNumber(clazz, map.values().iterator().next());
        }, sql, params);
    }

    /**
     * 查询多行多个字段
     */
    public List<Map<String, Object>> query(String sql, Object... params) throws SQLException {
        return query((map) -> map, sql, params);
    }


    /**
     * 数据库字段到实体类的映射
     */
    public interface JdbcMapping {

        /**
         * @param name 数据库字段
         * @return 实体类字段
         */
        String mapping(String name);

    }

    /**
     * 查询并转为类
     */
    public <T> List<T> query(Class<T> clazz, String sql, Object... params) throws SQLException {
        return query(clazz, null, sql, params);
    }

    public <T> List<T> query(final Class<T> clazz, final JdbcMapping jdbcMapping, String sql, Object... params) throws SQLException {
        Assert.notNull(clazz, "class can not null");
        return query((map) -> {
            try {
                T t = clazz.newInstance();
                for (String dbName : map.keySet()) {
                    String fieldName = (jdbcMapping == null) ? dbName : jdbcMapping.mapping(dbName);
                    Object value = map.get(dbName);
                    setFieldValue(t, fieldName, value);
                }
                return t;
            } catch (InstantiationException e) {
                throw new IllegalStateException("instantiation construct error", e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("can not access construct", e);
            }
        }, sql, params);
    }


    /**
     * 记录行处理
     */
    public interface JdbcRowHandler<T> {

        /**
         * 处理一行数据
         *
         * @param data 数据
         * @return 处理结果
         */
        T handleRow(Map<String, Object> data) throws SQLException;
    }

    /**
     * 查询
     *
     * @param handler 行处理
     */
    public <T> List<T> query(JdbcRowHandler<T> handler, String sql, Object... params) throws SQLException {
        Assert.notNull(handler, "handler can not null");
        Assert.hasText(sql, "sql can not empty");
        List<T> result = new ArrayList<>();
        Connection connection = getConnection();
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (!ObjectUtils.isEmpty(params)) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
            }
            resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    map.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
                result.add(handler.handleRow(map));
            }
        } finally {
            closeAuto(resultSet);
        }
        return result;
    }

    /**
     * 开始事务
     */
    public void startTransaction() throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        transactionState.set(true);
    }

    /**
     * 提交
     */
    public void commit() throws SQLException {
        try {
            Connection connection = getConnection();
            //commit出现异常，不设标志位，方便rollback
            connection.commit();
            connection.setAutoCommit(true);
            transactionState.set(false);
        } finally {
            closeAuto(null);
        }
    }

    /**
     * 回滚
     */
    public void rollback() throws SQLException {
        Connection connection = null;
        try {
            connection = getConnection();
            //rollback出现异常，退出事务
            connection.rollback();
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
            }
            transactionState.set(false);
            closeAuto(null);
        }
    }


    /**
     * 关闭连接
     */
    public void close() throws SQLException {
        Connection connection = connections.get();
        if (connection != null) {
            connection.close();
            connections.remove();
        }
    }

    /**
     * 关闭连接
     */
    private void closeAuto(ResultSet resultSet) throws SQLException {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } finally {
            if (autoClose && (transactionState.get() == null
                    || !transactionState.get())) {
                close();
            }
        }
    }


    /**
     * 设置值
     *
     * @param obj       转化后的对象
     * @param fieldName 字段名
     * @param value     数据库的值
     */
    private void setFieldValue(Object obj, String fieldName, Object value) {
        Field field = ReflectionUtils.findField(obj.getClass(), fieldName);
        if (field == null) {
            return;
        }
        Class<?> clazz = field.getType();
        if (value instanceof Date) {
            if (clazz == Date.class) {
                ReflectUtils.setObjectValue(obj, field, value);
            } else if (clazz == String.class) {
                ReflectUtils.setObjectValue(obj, field, value.toString());
            } else if (clazz == Long.class || clazz == long.class) {
                Date d = (Date) value;
                ReflectUtils.setObjectValue(obj, field, d.getTime());
            } else {
                throw new IllegalStateException("can not cast java.util.Date to " + clazz.getName());
            }
        } else {
            if (value != null) {
                if (ReflectUtils.isNumber(clazz) && ReflectUtils.isNumber(value.getClass())) {
                    value = ReflectUtils.convertNumber(clazz, value);
                }
            }
            ReflectUtils.setObjectValue(obj, field, value);
        }
    }


    private Connection getConnection() throws SQLException {
        if (connections.get() == null) {
            Connection connection = DriverManager.getConnection(url, username, password);
            connections.set(connection);
        }
        return connections.get();
    }


}
