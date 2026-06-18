package com.laptrinhjavaweb.orm.util;

import com.laptrinhjavaweb.orm.annotation.Column;
import com.laptrinhjavaweb.orm.annotation.Entity;
import com.laptrinhjavaweb.orm.annotation.Id;
import com.laptrinhjavaweb.orm.annotation.IdField;
import com.laptrinhjavaweb.orm.exception.TormException;

import java.lang.reflect.Field;

public class EntityUtil {
    public static boolean isAutoIncrement(Class<?> entityClass) {
        String idFieldName = getIdFieldName(entityClass);
        try {
            Field idField = ObjectAccessUtil.getFieldByName(entityClass, idFieldName);
            return idField.getAnnotation(Id.class).autoIncrement();
        } catch (NoSuchFieldException e) {
            throw new TormException("No field with name: " + idFieldName, e);
        }
    }

    public static String getTableName(Class<?> entityClass) {
        return entityClass.getAnnotation(Entity.class).tableName();
    }

    public static String getIdColumnName(Class<?> entityClass) {
        String idFieldName = getIdFieldName(entityClass);
        return getColumnName(entityClass, idFieldName);
    }

    public static String getIdFieldName(Class<?> entityClass) {
        return entityClass.getAnnotation(IdField.class).name();
    }

    public static String getColumnName(Class<?> entityClass, String fieldName) {
        try {
            return entityClass.getDeclaredField(fieldName).getAnnotation(Column.class).name();
        } catch (NoSuchFieldException e) {
            throw new TormException("No field with name: " + fieldName, e);
        }
    }

    public static Object getIdFieldData(Class<?> entityClass, Object entity) {
        try {
            String idFieldName = getIdFieldName(entityClass);
            Field idField = ObjectAccessUtil.getFieldByName(entityClass, idFieldName);
            return ObjectAccessUtil.getFieldData(entity, idField);
        } catch (Exception e) {
            throw new TormException(e);
        }
    }
}
