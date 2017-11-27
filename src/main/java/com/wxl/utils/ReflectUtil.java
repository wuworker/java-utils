package com.wxl.utils;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by wuxingle on 2017/10/21 0021.
 * 反射工具类
 */
public class ReflectUtil {


    /**
     * 获取属性值
     * 先找getter方法，再找属性值
     *
     * @param obj       对象
     * @param fieldName 属性名
     */
    public static Object getObjectValue(Object obj, String fieldName) {
        Assert.notNull(obj, "obj can not null");
        Assert.hasText(fieldName, "fieldName can not empty");
        Field field = ReflectionUtils.findField(obj.getClass(), fieldName);
        return getObjectValue(obj, field);
    }

    public static Object getObjectValue(Object obj, Field field) {
        Assert.notNull(obj, "obj can not null");
        Assert.notNull(field, "field can not null");
        Class<?> clazz = obj.getClass();
        String fieldName = field.getName();
        String methodName = "get" +
                fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1);
        try {
            Method method = ReflectionUtils.findMethod(clazz, methodName);
            if (method == null) {
                if (field.getType() == Boolean.class
                        || field.getType() == boolean.class) {
                    methodName = "is" + methodName.substring(3);
                    method = ReflectionUtils.findMethod(clazz, methodName);
                }
                if (method == null) {
                    field.setAccessible(true);
                    return field.get(obj);
                }
            }
            return method.invoke(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("can not access field:" + fieldName, e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("can not fond class", e);
        }
    }

    /**
     * 设置属性值
     * 先调setter方法，再设置属性
     *
     * @param obj       要设置的对象
     * @param fieldName 属性名
     * @param value     值
     */
    public static void setObjectValue(Object obj, String fieldName, Object value) {
        Assert.notNull(obj, "obj can not null");
        Assert.hasText(fieldName, "fieldName can not empty");
        Field field = ReflectionUtils.findField(obj.getClass(), fieldName);
        if (field != null) {
            setObjectValue(obj, field, value);
        }
    }

    public static void setObjectValue(Object obj, Field field, Object value) {
        Assert.notNull(obj, "obj can not null");
        Assert.notNull(field, "field can not null");
        if (value != null && !field.getType().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("the value [" + value.getClass().getName()
                    + "] can not cast to target field [" + field.getType().getName() + "]");
        }
        Class<?> clazz = obj.getClass();
        String name = field.getName();
        String methodName = "set"
                + name.substring(0, 1).toUpperCase()
                + name.substring(1);
        try {
            Method method = ReflectionUtils.findMethod(clazz, methodName, field.getType());
            if (method == null) {
                field.setAccessible(true);
                field.set(obj, value);
                return;
            }
            method.invoke(obj, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("can not access get method:" + methodName, e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("can not fond class", e);
        }
    }

    public static boolean isNumber(Class<?> clazz) {
        return Number.class.isAssignableFrom(clazz)
                || isPrimitiveNumber(clazz);
    }


    public static boolean isPrimitiveNumber(Class<?> clazz) {
        return clazz == byte.class
                || clazz == short.class
                || clazz == char.class
                || clazz == int.class
                || clazz == float.class
                || clazz == long.class
                || clazz == double.class;
    }

}
