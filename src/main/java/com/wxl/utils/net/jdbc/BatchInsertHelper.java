package com.wxl.utils.net.jdbc;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by wuxingle on 2017/12/11.
 * 随机批量插入数据
 * 没有事务管理
 */
@Slf4j
public class BatchInsertHelper {

    private static final int SINGLE_TASK_MAX_GENERATE_DATA = 5000;

    private static final int SINGLE_TASK_MAX_HANDLE = 10_000;

    private static final int INSERT_BATCH_MAX_LIMIT = 1000;

    private ForkJoinPool forkJoinPool;

    private JdbcUtils jdbcUtils;

    //单条线程生成数据大小
    @Getter
    private int singleTaskMaxGenerateData = SINGLE_TASK_MAX_GENERATE_DATA;

    //单条线程处理数据大小
    @Getter
    private int singleTaskMaxHandle = SINGLE_TASK_MAX_HANDLE;

    //一次性批量插入数据大小
    @Getter
    private int insertBatchMaxLimit = INSERT_BATCH_MAX_LIMIT;


    public BatchInsertHelper(String driver, String url, String username, String password) throws ClassNotFoundException {
        jdbcUtils = new JdbcUtils(driver, url, username, password, false);
        forkJoinPool = new ForkJoinPool();
    }


    /**
     * 批量插入数据
     *
     * @param tableName       表名
     * @param clazz           实体类(变量声明顺序和数据库一致)
     * @param count           (插入数)
     * @param entityGenerator 生成对象类
     * @return 插入数
     */
    public <T> int invokeBatchInsert(String tableName, Class<T> clazz, int count, EntityGenerator<T> entityGenerator) {
        return invokeBatchInsert(tableName, clazz, 1, count, entityGenerator);
    }

    public <T> int invokeBatchInsert(String tableName, Class<T> clazz, int startId, int count, EntityGenerator<T> entityGenerator) {
        log.debug("batch insert start,use config singleTaskMaxGenerateData={},singleTaskMaxHandle={},insertBatchMaxLimit={}",
                singleTaskMaxGenerateData, singleTaskMaxHandle, insertBatchMaxLimit);

        BlockingQueue<T> queue = new LinkedBlockingQueue<>(count);
        GenerateTask<T> generateTask = new GenerateTask<>(entityGenerator, queue, startId, startId + count);
        InsertTask<T> insertTask = new InsertTask<>(queue, clazz, tableName, startId, startId + count);

        ForkJoinTask<Integer> submit = forkJoinPool.submit(generateTask);
        Integer updateNum = forkJoinPool.invoke(insertTask);

        if (generateTask.isCompletedAbnormally()) {
            throw new RuntimeException(generateTask.getException());
        }
        if (insertTask.isCompletedAbnormally()) {
            throw new RuntimeException(insertTask.getException());
        }

        try {
            Integer generateNum = submit.get();
            if (!generateNum.equals(updateNum)) {
                log.info("generate data num : " + generateNum + " can not equals insert num:" + updateNum);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }

        return updateNum;
    }


    public void shutdown() throws SQLException {
        forkJoinPool.shutdown();
        jdbcUtils.close();
    }

    public void setSingleTaskMaxGenerateData(int singleTaskMaxGenerateData) {
        Assert.isTrue(singleTaskMaxGenerateData > 0 ,"singleTaskMaxGenerateData must > 0");
        this.singleTaskMaxGenerateData = singleTaskMaxGenerateData;
    }

    public void setSingleTaskMaxHandle(int singleTaskMaxHandle) {
        Assert.isTrue(singleTaskMaxHandle > 0 ,"singleTaskMaxHandle must > 0");
        this.singleTaskMaxHandle = singleTaskMaxHandle;
    }

    public void setInsertBatchMaxLimit(int insertBatchMaxLimit) {
        Assert.isTrue(insertBatchMaxLimit > 0 ,"insertBatchMaxLimit must > 0");
        this.insertBatchMaxLimit = insertBatchMaxLimit;
    }

    /**
     * 生成批量插入的sql
     */
    private String generateInsertsSQL(String tableName, int count, int fieldLength) {
        StringBuilder sqlsb = new StringBuilder("insert into " + tableName + " values(");
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < fieldLength; j++) {
                sqlsb.append(j == fieldLength - 1 ? "?),(" : "?,");
            }
        }
        return sqlsb.substring(0, sqlsb.length() - 2);
    }

    /**
     * 获取集合所有类的属性值
     */
    private <T> Object[] getAllFieldValue(Collection<T> collection, Field[] fields) {
        int fLen = fields.length;
        Object[] array = new Object[collection.size() * fLen];
        Iterator<T> it = collection.iterator();
        int i = 0;
        try {
            while (it.hasNext()) {
                T t = it.next();
                for (int j = 0; j < fLen; j++) {
                    fields[j].setAccessible(true);
                    array[i * fLen + j] = fields[j].get(t);
                }
                i++;
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        return array;
    }

    /**
     * 数据插入任务
     */
    private class InsertTask<T> extends RecursiveTask<Integer> {

        private BlockingQueue<T> queue;

        private Class<T> clazz;

        private String tableName;

        private int start;

        private int end;

        private int current;

        private Field[] fields;

        InsertTask(BlockingQueue<T> queue, Class<T> clazz, String tableName, int start, int end) {
            this.queue = queue;
            this.clazz = clazz;
            this.tableName = tableName;
            this.start = start;
            this.end = end;

            this.fields = clazz.getDeclaredFields();
        }

        @Override
        protected Integer compute() {
            if (end - start <= singleTaskMaxHandle) {
                int capacity = end - start;
                List<T> list = new ArrayList<>(insertBatchMaxLimit);
                try {
                    while (current < capacity) {
                        int max = capacity - current >= insertBatchMaxLimit ? insertBatchMaxLimit : capacity - current;
                        int num = queue.drainTo(list, max);
                        if (num == 0) {
                            Thread.yield();
                            continue;
                        }
                        String sql = generateInsertsSQL(tableName, num, fields.length);
                        Object[] params = getAllFieldValue(list, fields);
                        int update = jdbcUtils.update(sql, params);
                        if (update != num) {
                            log.error("the update num not equals generate num,except is {}, but update len is {}",num,update);
                            throw new IllegalStateException("data length is " + num + ",but update result is :" + update);
                        }
                        log.debug("insert " + tableName + " success:(" + (start + current) +
                                " to " + (start + current + num) + ")");
                        list.clear();
                        current += num;
                    }
                    return current;
                } catch (SQLException e) {
                    log.error("insert task execute error",e);
                    throw new IllegalArgumentException(e);
                } finally {
                    try {
                        jdbcUtils.close();
                    } catch (SQLException e) {
                        log.error("jdbc connection close error",e);
                    }
                }
            } else {
                int mid = (start + end) >>> 1;
                InsertTask<T> task1 = new InsertTask<>(queue, clazz, tableName, start, mid);
                InsertTask<T> task2 = new InsertTask<>(queue, clazz, tableName, mid, end);
                task1.fork();
                task2.fork();
                return task1.join() + task2.join();
            }
        }
    }

    /**
     * 数据生成任务
     */
    private class GenerateTask<T> extends RecursiveTask<Integer> {

        private EntityGenerator<T> generator;

        private BlockingQueue<T> queue;

        private int start;

        private int end;

        GenerateTask(EntityGenerator<T> generator, BlockingQueue<T> queue, int start, int end) {
            this.generator = generator;
            this.queue = queue;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            if (end - start <= singleTaskMaxGenerateData) {
                try {
                    for (int i = start; i < end; i++) {
                        T entity = generator.generateEntity(i);
                        queue.put(entity);
                    }
                } catch (InterruptedException e) {
                    throw new IllegalStateException("generate task was interrupted", e);
                }
                return end - start;
            }
            int mid = (end + start) >>> 1;
            GenerateTask<T> generateTask1 = new GenerateTask<>(generator, queue, start, mid);
            GenerateTask<T> generateTask2 = new GenerateTask<>(generator, queue, mid, end);
            generateTask1.fork();
            generateTask2.fork();
            return generateTask1.join() + generateTask2.join();
        }
    }


}




