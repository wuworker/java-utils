package com.wxl.utils;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * Created by wuxingle on 2017/10/21 0021.
 * 反射工具类
 */
public class ReflectUtils {


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


    /**
     * 把int数值转为对应的类
     */
    public static Object convertNumber(Class<?> clazz, int value) {
        Assert.notNull(clazz, "class can not null");
        Assert.notNull(value, "value can not null");
        if (isNumber(clazz)) {
            if (clazz == Long.class || clazz == long.class) {
                return (long) value;
            } else if (clazz == Integer.class || clazz == int.class) {
                return value;
            } else if (clazz == Short.class || clazz == short.class) {
                return (short) value;
            } else if (clazz == Character.class || clazz == char.class) {
                return (char) value;
            } else if (clazz == Byte.class || clazz == byte.class) {
                return (byte) value;
            } else if (clazz == Float.class || clazz == float.class) {
                return (float) value;
            } else if (clazz == Double.class || clazz == double.class) {
                return (double) value;
            } else if (clazz == BigDecimal.class) {
                return new BigDecimal(value);
            }
        }
        return null;
    }

    /**
     * 判断是否是数值
     */
    public static boolean isNumber(Class<?> clazz) {
        return Number.class.isAssignableFrom(clazz)
                || (clazz.isPrimitive() && clazz != boolean.class);
    }


}
