package com.wxl.utils.net.jdbc;

import com.wxl.utils.DateUtils;
import com.wxl.utils.RandomUtils;
import com.wxl.utils.ReflectUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Random;

/**
 * Created by wuxingle on 2017/12/11.
 * 实体类生成
 */
@Slf4j
public class EntityGenerator<T> {

    private static final String MIN_DATE = "2000-01-01 00:00:00";

    private static final int MIN_NUM = 0;
    private static final int MAX_NUM = 10_000;

    private static final int MIN_STR_LEN = 3;
    private static final int MAX_STR_LEN = 8;

    private Random random;

    private Class<T> clazz;

    private Field autoIncrementField;

    //随机生成数的范围
    @Setter
    @Getter
    private Date minDate;
    @Setter
    @Getter
    private Date maxDate;
    @Setter
    @Getter
    private int minNum = MIN_NUM;
    @Setter
    @Getter
    private int maxNum = MAX_NUM;
    @Setter
    @Getter
    private int minStrLen = MIN_STR_LEN;
    @Setter
    @Getter
    private int maxStrLen = MAX_STR_LEN;

    public EntityGenerator(Random random, Class<T> clazz) {
        this(random, clazz, "id");
    }

    public EntityGenerator(Random random, Class<T> clazz, String autoIncrementFiledName) {
        this.random = random;
        this.clazz = clazz;

        minDate = DateUtils.parse(MIN_DATE, "yyyy-MM-dd HH:mm:ss");
        maxDate = new Date();

        autoIncrementField = ReflectionUtils.findField(clazz, autoIncrementFiledName);
        if (autoIncrementField != null) {
            autoIncrementField.setAccessible(true);
        } else {
            log.warn("can not find this auto_increment field : {}", autoIncrementFiledName);
        }
    }

    /**
     * 生成实体类
     */
    public T generateEntity(int increment) {
        T entity = preGenerate(random, increment);
        if (entity != null) {
            return entity;
        }
        entity = generate(increment);

        entity = postGenerate(random, entity, increment);

        return entity;
    }

    /**
     * 默认的生成方法
     */
    protected T generate(int increment) {
        try {
            final T entity = clazz.newInstance();
            ReflectionUtils.doWithLocalFields(clazz, (f) -> {
                Object value = getRandomField(random, f, increment);
                f.setAccessible(true);
                f.set(entity, value);
            });
            if (autoIncrementField != null) {
                autoIncrementField.set(entity,
                        ReflectUtils.convertNumber(autoIncrementField.getType(), increment));
            }
            return entity;
        } catch (InstantiationException e) {
            throw new IllegalStateException(clazz.getName() + " has not default construct", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("construct can not access", e);
        }
    }

    /**
     * 生成前处理
     */
    protected T preGenerate(Random random, int increment) {
        return null;
    }

    /**
     * 生成后处理
     */
    protected T postGenerate(Random random, T entity, int increment) {
        return entity;
    }


    /**
     * 随机生成属性值
     */
    private Object getRandomField(Random random, Field field, int increment) {
        Class clazz = field.getType();
        if (clazz == String.class) {
            return RandomUtils.generateAbcNum(random, minStrLen, maxStrLen) + "t" + increment;
        } else if (clazz == Date.class) {
            return RandomUtils.generateDate(random, minDate, maxDate);
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return random.nextBoolean();
        } else if(ReflectUtils.isNumber(clazz)){
            return ReflectUtils.convertNumber(clazz, RandomUtils.generateInt(minNum, maxNum));
        } else {
            return null;
        }
    }


}




